package com.mapvendor.module.file.service;

import com.mapvendor.common.error.BusinessException;
import com.mapvendor.integration.storage.StoredFile;
import com.mapvendor.integration.storage.StorageService;
import com.mapvendor.module.file.dto.ImageResourceView;
import com.mapvendor.module.file.repository.FileResourceMapper;
import com.mapvendor.module.file.repository.FileResourceRow;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileResourceService {
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private final StorageService storage;
    private final FileResourceMapper mapper;
    public FileResourceService(StorageService storage, FileResourceMapper mapper) {
        this.storage = storage; this.mapper = mapper;
    }

    @Transactional
    public ImageResourceView upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid("上传文件不能为空");
        String original = file.getOriginalFilename();
        if (original != null && original.length() > 255) throw invalid("原始文件名过长");
        StoredFile stored = null;
        try (InputStream input = file.getInputStream()) {
            stored = storage.store(input, original, file.getContentType(), file.getSize());
            FileResourceRow row = new FileResourceRow();
            row.setStorageKey(stored.getStorageKey()); row.setPublicUrl(stored.getPublicUrl());
            row.setOriginalName(original); row.setMimeType(stored.getMimeType());
            row.setSizeBytes(stored.getSizeBytes()); row.setSha256(stored.getSha256());
            mapper.insert(row);
            AUDIT.info("admin_image_upload resourceId={} sizeBytes={} mimeType={} requestId={}",
                    row.getId(), row.getSizeBytes(), row.getMimeType(), MDC.get("requestId"));
            return new ImageResourceView(row.getId(), row.getPublicUrl(), row.getMimeType(), row.getSizeBytes());
        } catch (IOException ex) {
            cleanup(stored);
            throw new BusinessException("STORAGE_ERROR", "图片存储失败", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException ex) {
            cleanup(stored);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Download download(String storageKey) {
        FileResourceRow row = mapper.selectActiveByStorageKey(storageKey);
        if (row == null) throw notFound();
        Path path = storage.resolve(storageKey);
        if (!Files.isRegularFile(path)) throw notFound();
        try { return new Download(new UrlResource(path.toUri()), row.getMimeType(), row.getSizeBytes()); }
        catch (Exception ex) { throw notFound(); }
    }
    private void cleanup(StoredFile stored) {
        if (stored == null) return;
        try { storage.delete(stored.getStorageKey()); }
        catch (IOException ignored) { AUDIT.warn("orphan_image_cleanup_failed storageKey={}", stored.getStorageKey()); }
    }
    private BusinessException invalid(String message) { return new BusinessException("INVALID_IMAGE", message, HttpStatus.BAD_REQUEST); }
    private BusinessException notFound() { return new BusinessException("FILE_NOT_FOUND", "图片资源不存在", HttpStatus.NOT_FOUND); }
    public static final class Download {
        private final Resource resource; private final String mimeType; private final long size;
        public Download(Resource resource,String mimeType,long size){this.resource=resource;this.mimeType=mimeType;this.size=size;}
        public Resource getResource(){return resource;} public String getMimeType(){return mimeType;} public long getSize(){return size;}
    }
}
