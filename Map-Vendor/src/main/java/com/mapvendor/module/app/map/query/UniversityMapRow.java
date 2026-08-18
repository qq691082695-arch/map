package com.mapvendor.module.app.map.query;

public class UniversityMapRow {
    private Long id;
    private String name;
    private String intro;
    private String polygonJson;
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getPolygonJson() { return polygonJson; }
    public void setPolygonJson(String polygonJson) { this.polygonJson = polygonJson; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
