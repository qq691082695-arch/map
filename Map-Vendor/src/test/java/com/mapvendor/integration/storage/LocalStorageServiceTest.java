package com.mapvendor.integration.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapvendor.common.error.BusinessException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalStorageServiceTest {
    @TempDir
    Path directory;

    @Test
    void storesPngUnderGeneratedKey() throws Exception {
        byte[] png = png();
        LocalStorageService service = service();

        StoredFile stored = service.store(new ByteArrayInputStream(png), "map.png", "image/png", png.length);

        assertThat(stored.getStorageKey()).doesNotContain("map.png");
        assertThat(Files.exists(directory.resolve(stored.getStorageKey()))).isTrue();
        assertThat(stored.getSha256()).hasSize(64);
    }

    @Test
    void usesDetectedImageTypeWhenBrowserMimeAndExtensionAreWrong() throws Exception {
        byte[] png = png();

        StoredFile stored = service().store(new ByteArrayInputStream(png), "camera.jpg",
                "application/octet-stream", png.length);

        assertThat(stored.getMimeType()).isEqualTo("image/png");
        assertThat(stored.getStorageKey()).endsWith(".png");
        assertThat(Files.exists(directory.resolve(stored.getStorageKey()))).isTrue();
    }

    @Test
    void rejectsTruncatedImageSignature() {
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00};
        assertThatThrownBy(() -> service().store(new ByteArrayInputStream(png), "bad.png", "image/png", png.length))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsResolvedTraversalKey() {
        assertThatThrownBy(() -> service().resolve("../secret.png")).isInstanceOf(BusinessException.class);
    }

    @Test
    void acceptsValidJpegAndRemovesTrailingNonImageBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        byte[] jpeg = output.toByteArray();
        byte[] withTrailing = Arrays.copyOf(jpeg, jpeg.length + 4);
        withTrailing[jpeg.length] = 1; withTrailing[jpeg.length + 1] = 2;
        withTrailing[jpeg.length + 2] = 3; withTrailing[jpeg.length + 3] = 4;

        StoredFile stored = service().store(new ByteArrayInputStream(withTrailing), "photo.jpg",
                "image/jpeg", withTrailing.length);

        assertThat(stored.getSizeBytes()).isEqualTo(jpeg.length);
        assertThat(Files.readAllBytes(directory.resolve(stored.getStorageKey()))).isEqualTo(jpeg);
    }

    private byte[] png() {
        return Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
    }

    @Test
    void rejectsPathTraversalFilename() {
        assertThatThrownBy(() -> service().store(new ByteArrayInputStream(new byte[8]),
                "../bad.png", "image/png", 8)).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsDisguisedMime() {
        byte[] script = "<script>bad</script>".getBytes();
        assertThatThrownBy(() -> service().store(new ByteArrayInputStream(script),
                "bad.png", "image/png", script.length)).isInstanceOf(BusinessException.class);
    }

    private LocalStorageService service() {
        StorageProperties properties = new StorageProperties();
        properties.setRoot(directory.toString());
        properties.setPublicBaseUrl("/files");
        properties.setMaxFileSizeBytes(1024);
        return new LocalStorageService(properties);
    }
}
