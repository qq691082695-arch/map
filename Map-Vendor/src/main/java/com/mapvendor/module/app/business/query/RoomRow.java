package com.mapvendor.module.app.business.query;

public class RoomRow {
    private Long id;
    private String name;
    private String bedSpec;
    private String description;
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBedSpec() { return bedSpec; }
    public void setBedSpec(String bedSpec) { this.bedSpec = bedSpec; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}