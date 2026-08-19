package com.mapvendor.module.app.business.dto;

public class BusinessDetail {
    private final BusinessCommon common;
    private final Object detail;

    public BusinessDetail(BusinessCommon common, Object detail) {
        this.common = common;
        this.detail = detail;
    }

    public BusinessCommon getCommon() { return common; }
    public Object getDetail() { return detail; }
}