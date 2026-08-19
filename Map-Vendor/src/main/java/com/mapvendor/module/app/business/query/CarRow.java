package com.mapvendor.module.app.business.query;

public class CarRow {
    private Long id;
    private String model;
    private Integer seatNum;
    private String description;
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getSeatNum() { return seatNum; }
    public void setSeatNum(Integer seatNum) { this.seatNum = seatNum; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}