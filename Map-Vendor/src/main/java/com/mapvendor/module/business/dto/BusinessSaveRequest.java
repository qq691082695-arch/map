package com.mapvendor.module.business.dto;

import com.mapvendor.module.order.domain.BusinessType;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BusinessSaveRequest {
    @NotBlank @Size(max = 128)
    private String name;
    @NotBlank @Size(max = 255)
    private String address;
    @NotNull @DecimalMin("-180") @DecimalMax("180")
    private BigDecimal longitude;
    @NotNull @DecimalMin("-90") @DecimalMax("90")
    private BigDecimal latitude;
    @NotNull
    private BusinessType businessType;
    private String intro;
    @Size(max = 64)
    private String foodContactName;
    @Size(max = 32)
    private String foodContactPhone;
    @Size(max = 500)
    private String foodRecommendedDishes;
    @Size(max = 20)
    private List<@NotNull Long> imageResourceIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getFoodContactName() { return foodContactName; }
    public void setFoodContactName(String foodContactName) { this.foodContactName = foodContactName; }
    public String getFoodContactPhone() { return foodContactPhone; }
    public void setFoodContactPhone(String foodContactPhone) { this.foodContactPhone = foodContactPhone; }
    public String getFoodRecommendedDishes() { return foodRecommendedDishes; }
    public void setFoodRecommendedDishes(String foodRecommendedDishes) { this.foodRecommendedDishes = foodRecommendedDishes; }
    public List<Long> getImageResourceIds() { return imageResourceIds; }
    public void setImageResourceIds(List<Long> imageResourceIds) { this.imageResourceIds = imageResourceIds; }
}
