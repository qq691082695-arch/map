package com.mapvendor.module.statistics.repository;

public class StatisticsRow {
    private Long businessId;
    private String businessNameSnapshot;
    private String businessType;
    private long pendingCount;
    private long confirmedCount;
    private long cancelledCount;
    private long totalCount;

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public String getBusinessNameSnapshot() { return businessNameSnapshot; }
    public void setBusinessNameSnapshot(String businessNameSnapshot) { this.businessNameSnapshot = businessNameSnapshot; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public long getPendingCount() { return pendingCount; }
    public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }
    public long getConfirmedCount() { return confirmedCount; }
    public void setConfirmedCount(long confirmedCount) { this.confirmedCount = confirmedCount; }
    public long getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
}
