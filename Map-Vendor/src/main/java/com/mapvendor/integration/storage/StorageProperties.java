package com.mapvendor.integration.storage;

import java.nio.file.Paths;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "map-vendor.storage")
public class StorageProperties {
    private String root = Paths.get("data", "images").toString();
    private String publicBaseUrl = "/files";
    private long maxFileSizeBytes = 10L * 1024L * 1024L;
    private long minimumFreeBytes = 5L * 1024L * 1024L * 1024L;

    public Path getRoot() { return Paths.get(root); }
    public void setRoot(String root) { this.root = root; }
    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }
    public long getMinimumFreeBytes() { return minimumFreeBytes; }
    public void setMinimumFreeBytes(long minimumFreeBytes) { this.minimumFreeBytes = minimumFreeBytes; }
}
