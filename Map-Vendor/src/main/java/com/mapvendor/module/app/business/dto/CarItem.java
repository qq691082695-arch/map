package com.mapvendor.module.app.business.dto;

public class CarItem {
    private final Long id;
    private final String model;
    private final Integer seatNum;
    private final String description;
    private final String imageUrl;

    public CarItem(Long id, String model, Integer seatNum, String description, String imageUrl) {
        this.id = id;
        this.model = model;
        this.seatNum = seatNum;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public String getModel() { return model; }
    public Integer getSeatNum() { return seatNum; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}