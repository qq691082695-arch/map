package com.mapvendor.module.app.business.dto;

public class RoomItem {
    private final Long id;
    private final String name;
    private final String bedSpec;
    private final String description;
    private final String imageUrl;

    public RoomItem(Long id, String name, String bedSpec, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.bedSpec = bedSpec;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBedSpec() { return bedSpec; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}