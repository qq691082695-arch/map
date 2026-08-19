package com.mapvendor.module.app.business.dto;

import com.mapvendor.module.order.domain.BusinessType;
import java.util.List;

public class TravelDetail {
    private final BusinessType kind;
    private final List<CarItem> cars;

    public TravelDetail(BusinessType kind, List<CarItem> cars) {
        this.kind = kind;
        this.cars = cars;
    }

    public BusinessType getKind() { return kind; }
    public List<CarItem> getCars() { return cars; }
}