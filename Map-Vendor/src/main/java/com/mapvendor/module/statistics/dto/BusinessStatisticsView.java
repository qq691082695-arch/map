package com.mapvendor.module.statistics.dto;

import com.mapvendor.module.order.domain.BusinessType;

public class BusinessStatisticsView extends StatusCounts {
    private final long businessId;
    private final String businessNameSnapshot;
    private final BusinessType businessType;

    public BusinessStatisticsView(long businessId, String businessNameSnapshot, BusinessType businessType,
            long pendingCount, long confirmedCount, long cancelledCount, long totalCount) {
        super(pendingCount, confirmedCount, cancelledCount, totalCount);
        this.businessId = businessId;
        this.businessNameSnapshot = businessNameSnapshot;
        this.businessType = businessType;
    }
    public long getBusinessId() { return businessId; }
    public String getBusinessNameSnapshot() { return businessNameSnapshot; }
    public BusinessType getBusinessType() { return businessType; }
}
