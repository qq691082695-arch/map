package com.mapvendor.module.app.map.query;

import java.math.BigDecimal;

public class BusinessMapRow {
    private Long id;
    private String name;
    private String businessType;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String intro;
    private String coverImageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
}
