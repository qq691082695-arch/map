package com.mapvendor.module.app.business.query;

import java.math.BigDecimal;

public class BusinessDetailRow {
    private Long id;
    private String name;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String businessType;
    private String intro;
    private String foodContactName;
    private String foodContactPhone;
    private String foodRecommendedDishes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getFoodContactName() { return foodContactName; }
    public void setFoodContactName(String foodContactName) { this.foodContactName = foodContactName; }
    public String getFoodContactPhone() { return foodContactPhone; }
    public void setFoodContactPhone(String foodContactPhone) { this.foodContactPhone = foodContactPhone; }
    public String getFoodRecommendedDishes() { return foodRecommendedDishes; }
    public void setFoodRecommendedDishes(String foodRecommendedDishes) { this.foodRecommendedDishes = foodRecommendedDishes; }
}