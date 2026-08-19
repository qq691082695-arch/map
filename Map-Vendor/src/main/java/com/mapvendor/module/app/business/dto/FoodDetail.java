package com.mapvendor.module.app.business.dto;

import com.mapvendor.module.order.domain.BusinessType;
import java.util.List;

public class FoodDetail {
    private final BusinessType kind;
    private final String contactName;
    private final String contactPhone;
    private final String recommendedDishes;
    private final List<DishItem> dishes;

    public FoodDetail(BusinessType kind, String contactName, String contactPhone,
                      String recommendedDishes, List<DishItem> dishes) {
        this.kind = kind;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.recommendedDishes = recommendedDishes;
        this.dishes = dishes;
    }

    public BusinessType getKind() { return kind; }
    public String getContactName() { return contactName; }
    public String getContactPhone() { return contactPhone; }
    public String getRecommendedDishes() { return recommendedDishes; }
    public List<DishItem> getDishes() { return dishes; }
}