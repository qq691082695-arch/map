package com.mapvendor.integration.export;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "map-vendor.export")
public class OrderExportProperties {
    private int maxRows = 10000;
    private int maxDateRangeDays = 366;
    private int fetchSize = 500;

    public int getMaxRows() { return maxRows; }
    public void setMaxRows(int maxRows) { this.maxRows = maxRows; }
    public int getMaxDateRangeDays() { return maxDateRangeDays; }
    public void setMaxDateRangeDays(int maxDateRangeDays) { this.maxDateRangeDays = maxDateRangeDays; }
    public int getFetchSize() { return fetchSize; }
    public void setFetchSize(int fetchSize) { this.fetchSize = fetchSize; }
}
