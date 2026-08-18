package com.mapvendor.module.university.repository;

public class UniversityImageRow {
    private Long resourceId;
    private String publicUrl;
    private Integer sortNo;

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
