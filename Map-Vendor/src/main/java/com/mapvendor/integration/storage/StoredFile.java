package com.mapvendor.integration.storage;

public final class StoredFile {
    private final String storageKey;
    private final String publicUrl;
    private final String mimeType;
    private final long sizeBytes;
    private final String sha256;

    public StoredFile(String storageKey, String publicUrl, String mimeType, long sizeBytes, String sha256) {
        this.storageKey = storageKey;
        this.publicUrl = publicUrl;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
    }

    public String getStorageKey() { return storageKey; }
    public String getPublicUrl() { return publicUrl; }
    public String getMimeType() { return mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getSha256() { return sha256; }
}
