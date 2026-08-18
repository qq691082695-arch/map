package com.mapvendor.common.api;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class PageQuery {
    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int pageSize = 20;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
