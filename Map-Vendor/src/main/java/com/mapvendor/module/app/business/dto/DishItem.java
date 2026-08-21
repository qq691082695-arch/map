package com.mapvendor.module.app.business.dto;

public class DishItem {
    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final Boolean recommended;

    public DishItem(Long id, String name, String description, String imageUrl, Boolean recommended) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.recommended = recommended;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getRecommended() { return recommended; }
}
