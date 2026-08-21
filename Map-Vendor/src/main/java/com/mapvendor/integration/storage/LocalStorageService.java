package com.mapvendor.integration.storage;

import com.mapvendor.common.error.BusinessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.CRC32;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LocalStorageService implements StorageService {
    private static final Logger LOG = LoggerFactory.getLogger(LocalStorageService.class);
    private static final Map<String, String> EXTENSIONS = new HashMap<String, String>();

    static {
        EXTENSIONS.put("image/jpeg", ".jpg");
        EXTENSIONS.put("image/png", ".png");
        EXTENSIONS.put("image/webp", ".webp");
    }

    private final StorageProperties properties;

    public LocalStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredFile store(InputStream input, String originalFilename, String declaredContentType, long size)
            throws IOException {
        validateMetadata(originalFilename, size);
        byte[] content = readLimited(input, properties.getMaxFileSizeBytes());
        if (content.length != size) {
            throw invalid("文件大小与声明不一致");
        }
        String detectedType = detectType(content);
        content = normalizeContent(content, detectedType);

        LocalDate now = LocalDate.now();
        String relativeDirectory = now.getYear() + "/" + String.format(Locale.ROOT, "%02d", now.getMonthValue());
        String filename = UUID.randomUUID().toString().replace("-", "") + EXTENSIONS.get(detectedType);
        String storageKey = relativeDirectory + "/" + filename;

        Path root = properties.getRoot().toAbsolutePath().normalize();
        Path directory = root.resolve(relativeDirectory).normalize();
        Path target = directory.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw invalid("非法存储路径");
        }
        Files.createDirectories(directory);
        long usable = Files.getFileStore(directory).getUsableSpace();
        if (usable < properties.getMinimumFreeBytes()) {
            LOG.warn("image_storage_capacity_low usableBytes={} thresholdBytes={}", usable,
                    properties.getMinimumFreeBytes());
        }
        if (usable < size) {
            throw new BusinessException("STORAGE_CAPACITY_EXHAUSTED", "图片存储空间不足",
                    HttpStatus.INSUFFICIENT_STORAGE);
        }
        Path temporary = Files.createTempFile(directory, ".upload-", ".tmp");
        try {
            Files.write(temporary, content);
            moveAtomically(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }

        String baseUrl = properties.getPublicBaseUrl().replaceAll("/+$", "");
        return new StoredFile(storageKey, baseUrl + "/" + storageKey, detectedType,
                content.length, sha256(content));
    }

    @Override
    public Path resolve(String storageKey) {
        if (!StringUtils.hasText(storageKey) || storageKey.contains("\\") || storageKey.contains("..")
                || storageKey.startsWith("/")) {
            throw invalid("非法存储路径");
        }
        Path root = properties.getRoot().toAbsolutePath().normalize();
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw invalid("非法存储路径");
        }
        return target;
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(resolve(storageKey));
    }

    private void validateMetadata(String originalFilename, long size) {
        if (!StringUtils.hasText(originalFilename) || originalFilename.contains("/")
                || originalFilename.contains("\\") || originalFilename.contains("..")) {
            throw invalid("非法文件名");
        }
        if (size <= 0 || size > properties.getMaxFileSizeBytes()) {
            throw invalid("文件大小不合法");
        }
    }

    private byte[] readLimited(InputStream input, long maximum) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        long total = 0;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > maximum) {
                throw invalid("文件超过大小限制");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private String detectType(byte[] bytes) {
        if (isJpeg(bytes)) {
            return "image/jpeg";
        }
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        if (bytes.length >= 33 && Arrays.equals(png, Arrays.copyOf(bytes, png.length)) && isPng(bytes)) {
            return "image/png";
        }
        if (isWebp(bytes)) {
            return "image/webp";
        }
        throw invalid("无法识别图片真实类型");
    }

    private boolean isPng(byte[] bytes) {
        int offset = 8;
        boolean first = true;
        while (offset + 12 <= bytes.length) {
            long length = bigEndianInt(bytes, offset);
            if (length > Integer.MAX_VALUE || offset + 12L + length > bytes.length) return false;
            int dataLength = (int) length;
            String type = asciiValue(bytes, offset + 4, 4);
            if (first && (!"IHDR".equals(type) || dataLength != 13)) return false;
            CRC32 crc = new CRC32();
            crc.update(bytes, offset + 4, 4 + dataLength);
            if (crc.getValue() != bigEndianInt(bytes, offset + 8 + dataLength)) return false;
            offset += 12 + dataLength;
            first = false;
            if ("IEND".equals(type)) return dataLength == 0 && offset == bytes.length;
        }
        return false;
    }

    private boolean isJpeg(byte[] bytes) {
        return jpegEndOffset(bytes) > 0;
    }

    private int jpegEndOffset(byte[] bytes) {
        if (bytes.length < 20 || (bytes[0] & 0xff) != 0xff || (bytes[1] & 0xff) != 0xd8) return -1;
        boolean hasFrame = false;
        boolean hasScan = false;
        int offset = 2;
        while (offset < bytes.length) {
            if ((bytes[offset] & 0xff) != 0xff) {
                if (!hasScan) return -1;
                offset++;
                continue;
            }
            while (offset < bytes.length && (bytes[offset] & 0xff) == 0xff) offset++;
            if (offset >= bytes.length) return -1;
            int marker = bytes[offset++] & 0xff;
            if (marker == 0x00 && hasScan) continue;
            if (marker == 0xd9) return hasFrame && hasScan ? offset : -1;
            if (marker == 0x01 || marker >= 0xd0 && marker <= 0xd7) continue;
            if (offset + 2 > bytes.length) return -1;
            int length = ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
            if (length < 2 || offset + length > bytes.length) return -1;
            if (marker >= 0xc0 && marker <= 0xc3 || marker >= 0xc5 && marker <= 0xc7
                    || marker >= 0xc9 && marker <= 0xcb || marker >= 0xcd && marker <= 0xcf) hasFrame = true;
            if (marker == 0xda) hasScan = true;
            offset += length;
        }
        return -1;
    }

    private byte[] normalizeContent(byte[] content, String mimeType) {
        if (!"image/jpeg".equals(mimeType)) return content;
        int end = jpegEndOffset(content);
        if (end > 0 && end < content.length) {
            LOG.info("jpeg_trailing_bytes_removed count={}", content.length - end);
            return Arrays.copyOf(content, end);
        }
        return content;
    }

    private boolean isWebp(byte[] bytes) {
        if (bytes.length < 20 || !ascii(bytes, 0, "RIFF") || !ascii(bytes, 8, "WEBP")
                || littleEndianInt(bytes, 4) + 8L != bytes.length) return false;
        int offset = 12;
        boolean imageChunk = false;
        while (offset + 8 <= bytes.length) {
            String type = asciiValue(bytes, offset, 4);
            long length = littleEndianInt(bytes, offset + 4);
            long next = offset + 8L + length + (length & 1L);
            if (next > bytes.length) return false;
            if ("VP8 ".equals(type) || "VP8L".equals(type) || "VP8X".equals(type)) imageChunk = true;
            offset = (int) next;
        }
        return imageChunk && offset == bytes.length;
    }

    private long bigEndianInt(byte[] bytes, int offset) {
        return (((long) bytes[offset] & 0xff) << 24) | (((long) bytes[offset + 1] & 0xff) << 16)
                | (((long) bytes[offset + 2] & 0xff) << 8) | ((long) bytes[offset + 3] & 0xff);
    }

    private String asciiValue(byte[] bytes, int offset, int length) {
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) value.append((char) (bytes[offset + i] & 0xff));
        return value.toString();
    }

    private long littleEndianInt(byte[] bytes, int offset) {
        return ((long) bytes[offset] & 0xff) | (((long) bytes[offset + 1] & 0xff) << 8)
                | (((long) bytes[offset + 2] & 0xff) << 16) | (((long) bytes[offset + 3] & 0xff) << 24);
    }

    private boolean ascii(byte[] bytes, int offset, String value) {
        for (int i = 0; i < value.length(); i++) {
            if (bytes[offset + i] != (byte) value.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private String sha256(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder value = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                value.append(String.format(Locale.ROOT, "%02x", item & 0xff));
            }
            return value.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target);
        }
    }

    private BusinessException invalid(String message) {
        return new BusinessException("INVALID_IMAGE", message, HttpStatus.BAD_REQUEST);
    }
}
