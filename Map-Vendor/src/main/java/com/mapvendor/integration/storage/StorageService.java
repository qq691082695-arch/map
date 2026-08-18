package com.mapvendor.integration.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface StorageService {
    StoredFile store(InputStream input, String originalFilename, String declaredContentType, long size) throws IOException;
    Path resolve(String storageKey);
    void delete(String storageKey) throws IOException;
}
