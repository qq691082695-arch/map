package com.mapvendor.common.api;

import java.util.List;

public final class PageResult<T> {
    private final List<T> items;
    private final long total;
    private final int page;
    private final int pageSize;

    public PageResult(List<T> items, long total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getItems() { return items; }
    public long getTotal() { return total; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
}
