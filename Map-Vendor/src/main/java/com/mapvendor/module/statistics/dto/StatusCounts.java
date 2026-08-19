package com.mapvendor.module.statistics.dto;

public class StatusCounts {
    private final long pendingCount;
    private final long confirmedCount;
    private final long cancelledCount;
    private final long totalCount;

    public StatusCounts(long pendingCount, long confirmedCount, long cancelledCount, long totalCount) {
        this.pendingCount = pendingCount;
        this.confirmedCount = confirmedCount;
        this.cancelledCount = cancelledCount;
        this.totalCount = totalCount;
    }
    public long getPendingCount() { return pendingCount; }
    public long getConfirmedCount() { return confirmedCount; }
    public long getCancelledCount() { return cancelledCount; }
    public long getTotalCount() { return totalCount; }
}
