package com.mapvendor.module.app.map.dto;

import java.util.List;

public class UniversityMapItem {
    private final Long id;
    private final String name;
    private final String intro;
    private final List<GeoPoint> polygonPoints;
    private final List<String> imageUrls;

    public UniversityMapItem(Long id, String name, String intro, List<GeoPoint> polygonPoints,
                             List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.polygonPoints = polygonPoints;
        this.imageUrls = imageUrls;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getIntro() { return intro; }
    public List<GeoPoint> getPolygonPoints() { return polygonPoints; }
    public List<String> getImageUrls() { return imageUrls; }
}
