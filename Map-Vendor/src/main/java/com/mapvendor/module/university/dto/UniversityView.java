package com.mapvendor.module.university.dto;

import com.mapvendor.module.app.map.dto.GeoPoint;
import com.mapvendor.module.university.domain.UniversityStatus;
import java.time.LocalDateTime;
import java.util.List;

public final class UniversityView {
    private final long id;
    private final String name;
    private final String intro;
    private final List<GeoPoint> polygonPoints;
    private final UniversityStatus status;
    private final List<UniversityImage> images;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UniversityView(long id, String name, String intro, List<GeoPoint> polygonPoints,
            UniversityStatus status, List<UniversityImage> images, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.polygonPoints = polygonPoints;
        this.status = status;
        this.images = images;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getIntro() { return intro; }
    public List<GeoPoint> getPolygonPoints() { return polygonPoints; }
    public UniversityStatus getStatus() { return status; }
    public List<UniversityImage> getImages() { return images; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
