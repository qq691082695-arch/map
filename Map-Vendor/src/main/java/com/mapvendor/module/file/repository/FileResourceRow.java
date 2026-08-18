package com.mapvendor.module.file.repository;

public class FileResourceRow {
    private Long id;
    private String storageKey;
    private String publicUrl;
    private String originalName;
    private String mimeType;
    private long sizeBytes;
    private String sha256;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
}
