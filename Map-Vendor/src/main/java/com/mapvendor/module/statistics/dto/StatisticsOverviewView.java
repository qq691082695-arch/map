package com.mapvendor.module.statistics.dto;

import java.time.LocalDate;
import java.util.List;

public class StatisticsOverviewView {
    private final LocalDate serviceDateFrom;
    private final LocalDate serviceDateTo;
    private final StatusCounts total;
    private final List<BusinessStatisticsView> businesses;

    public StatisticsOverviewView(LocalDate serviceDateFrom, LocalDate serviceDateTo, StatusCounts total,
            List<BusinessStatisticsView> businesses) {
        this.serviceDateFrom = serviceDateFrom;
        this.serviceDateTo = serviceDateTo;
        this.total = total;
        this.businesses = businesses;
    }
    public LocalDate getServiceDateFrom() { return serviceDateFrom; }
    public LocalDate getServiceDateTo() { return serviceDateTo; }
    public StatusCounts getTotal() { return total; }
    public List<BusinessStatisticsView> getBusinesses() { return businesses; }
}
