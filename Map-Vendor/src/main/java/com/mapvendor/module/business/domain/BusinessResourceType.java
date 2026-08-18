package com.mapvendor.module.business.domain;

import com.mapvendor.module.order.domain.BusinessType;

public enum BusinessResourceType {
    CARS("business_travel_car", BusinessType.TRAVEL),
    ROOMS("business_hotel_room", BusinessType.HOTEL),
    DISHES("business_food_dish", BusinessType.FOOD);

    private final String table;
    private final BusinessType businessType;
    BusinessResourceType(String table, BusinessType businessType) { this.table = table; this.businessType = businessType; }
    public String getTable() { return table; }
    public BusinessType getBusinessType() { return businessType; }
}
