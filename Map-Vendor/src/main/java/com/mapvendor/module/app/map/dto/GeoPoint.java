package com.mapvendor.module.app.map.dto;

import java.math.BigDecimal;

public class GeoPoint {
    private BigDecimal latitude;
    private BigDecimal longitude;

    public GeoPoint() {
    }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}
