package com.mapvendor.module.app.map.dto;

import com.mapvendor.module.order.domain.BusinessType;
import java.math.BigDecimal;

public class BusinessMapItem {
    private final Long id;
    private final String name;
    private final BusinessType businessType;
    private final String address;
    private final BigDecimal longitude;
    private final BigDecimal latitude;
    private final String intro;
    private final String coverImageUrl;

    public BusinessMapItem(Long id, String name, BusinessType businessType, String address,
                           BigDecimal longitude, BigDecimal latitude, String intro, String coverImageUrl) {
        this.id = id;
        this.name = name;
        this.businessType = businessType;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.intro = intro;
        this.coverImageUrl = coverImageUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BusinessType getBusinessType() { return businessType; }
    public String getAddress() { return address; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public String getIntro() { return intro; }
    public String getCoverImageUrl() { return coverImageUrl; }
}
