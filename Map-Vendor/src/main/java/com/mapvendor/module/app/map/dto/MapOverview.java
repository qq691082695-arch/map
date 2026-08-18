package com.mapvendor.module.app.map.dto;

import java.util.List;

public class MapOverview {
    private final List<UniversityMapItem> universities;
    private final List<BusinessMapItem> businesses;

    public MapOverview(List<UniversityMapItem> universities, List<BusinessMapItem> businesses) {
        this.universities = universities;
        this.businesses = businesses;
    }

    public List<UniversityMapItem> getUniversities() { return universities; }
    public List<BusinessMapItem> getBusinesses() { return businesses; }
}
