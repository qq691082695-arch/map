package com.mapvendor.module.business.dto;

import com.mapvendor.module.business.domain.BusinessStatus;
import com.mapvendor.module.order.domain.BusinessType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.mapvendor.module.university.dto.UniversityImage;

public class BusinessView {
    private final long id;
    private final String name;
    private final String address;
    private final BigDecimal longitude;
    private final BigDecimal latitude;
    private final BusinessType businessType;
    private final String intro;
    private final String foodContactName;
    private final String foodContactPhone;
    private final String foodRecommendedDishes;
    private final BusinessStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<UniversityImage> images;

    public BusinessView(long id, String name, String address, BigDecimal longitude, BigDecimal latitude,
            BusinessType businessType, String intro, String foodContactName, String foodContactPhone,
            String foodRecommendedDishes, BusinessStatus status, List<UniversityImage> images,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.name = name; this.address = address; this.longitude = longitude;
        this.latitude = latitude; this.businessType = businessType; this.intro = intro;
        this.foodContactName = foodContactName; this.foodContactPhone = foodContactPhone;
        this.foodRecommendedDishes = foodRecommendedDishes; this.status = status;
        this.images = images; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }
    public long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public BusinessType getBusinessType() { return businessType; }
    public String getIntro() { return intro; }
    public String getFoodContactName() { return foodContactName; }
    public String getFoodContactPhone() { return foodContactPhone; }
    public String getFoodRecommendedDishes() { return foodRecommendedDishes; }
    public BusinessStatus getStatus() { return status; }
    public List<UniversityImage> getImages() { return images; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
