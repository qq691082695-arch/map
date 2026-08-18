package com.mapvendor.module.university.dto;

public final class UniversityImage {
    private final long resourceId;
    private final String url;
    private final int sortNo;

    public UniversityImage(long resourceId, String url, int sortNo) {
        this.resourceId = resourceId;
        this.url = url;
        this.sortNo = sortNo;
    }

    public long getResourceId() { return resourceId; }
    public String getUrl() { return url; }
    public int getSortNo() { return sortNo; }
}
