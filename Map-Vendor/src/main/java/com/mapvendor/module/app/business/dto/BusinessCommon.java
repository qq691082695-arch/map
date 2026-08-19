package com.mapvendor.module.app.business.dto;

import com.mapvendor.module.order.domain.BusinessType;
import java.math.BigDecimal;
import java.util.List;

public class BusinessCommon {
    private final long id;
    private final String name;
    private final BusinessType businessType;
    private final String address;
    private final BigDecimal longitude;
    private final BigDecimal latitude;
    private final String intro;
    private final List<String> imageUrls;

    public BusinessCommon(long id, String name, BusinessType businessType, String address,
                          BigDecimal longitude, BigDecimal latitude, String intro, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.businessType = businessType;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.intro = intro;
        this.imageUrls = imageUrls;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public BusinessType getBusinessType() { return businessType; }
    public String getAddress() { return address; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public String getIntro() { return intro; }
    public List<String> getImageUrls() { return imageUrls; }
}