package com.mapvendor.module.file.dto;

public class ImageResourceView {
    private final long resourceId;
    private final String url;
    private final String mimeType;
    private final long sizeBytes;

    public ImageResourceView(long resourceId, String url, String mimeType, long sizeBytes) {
        this.resourceId = resourceId;
        this.url = url;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
    }
    public long getResourceId() { return resourceId; }
    public String getUrl() { return url; }
    public String getMimeType() { return mimeType; }
    public long getSizeBytes() { return sizeBytes; }
}
