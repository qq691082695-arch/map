package com.mapvendor.module.university.dto;

import com.mapvendor.module.app.map.dto.GeoPoint;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class UniversitySaveRequest {
    @NotBlank
    @Size(max = 128)
    private String name;

    private String intro;

    @Valid
    @NotEmpty
    private List<GeoPoint> polygonPoints = new ArrayList<GeoPoint>();

    @Size(max = 20)
    private List<Long> imageResourceIds = new ArrayList<Long>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public List<GeoPoint> getPolygonPoints() { return polygonPoints; }
    public void setPolygonPoints(List<GeoPoint> polygonPoints) { this.polygonPoints = polygonPoints; }
    public List<Long> getImageResourceIds() { return imageResourceIds; }
    public void setImageResourceIds(List<Long> imageResourceIds) { this.imageResourceIds = imageResourceIds; }
}
