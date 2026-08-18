package com.mapvendor.module.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mapvendor.integration.storage.StoredFile;
import com.mapvendor.integration.storage.StorageService;
import com.mapvendor.module.file.repository.FileResourceMapper;
import com.mapvendor.module.file.service.FileResourceService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class FileResourceServiceTest {
    @Test
    void closesMultipartTemporaryFileStreamAfterUpload() throws Exception {
        AtomicBoolean closed = new AtomicBoolean(false);
        InputStream input = new ByteArrayInputStream(new byte[] {1}) {
            @Override public void close() throws IOException { closed.set(true); super.close(); }
        };
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1L);
        when(file.getInputStream()).thenReturn(input);
        StorageService storage = mock(StorageService.class);
        when(storage.store(any(InputStream.class), eq("image.png"), eq("image/png"), eq(1L)))
                .thenReturn(new StoredFile("2026/08/a.png", "/files/2026/08/a.png", "image/png", 1L,
                        "0000000000000000000000000000000000000000000000000000000000000000"));
        FileResourceMapper mapper = mock(FileResourceMapper.class);
        doAnswer(invocation -> {
            com.mapvendor.module.file.repository.FileResourceRow row = invocation.getArgument(0);
            row.setId(1L);
            return 1;
        }).when(mapper).insert(any());

        new FileResourceService(storage, mapper).upload(file);

        assertThat(closed).isTrue();
        verify(mapper).insert(any());
    }
}
