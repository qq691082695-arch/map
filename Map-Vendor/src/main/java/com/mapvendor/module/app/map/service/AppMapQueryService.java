package com.mapvendor.module.app.map.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapvendor.module.app.map.dto.BusinessMapItem;
import com.mapvendor.module.app.map.dto.GeoPoint;
import com.mapvendor.module.app.map.dto.MapOverview;
import com.mapvendor.module.app.map.dto.UniversityMapItem;
import com.mapvendor.module.app.map.query.AppMapQueryMapper;
import com.mapvendor.module.app.map.query.BusinessMapRow;
import com.mapvendor.module.app.map.query.UniversityMapRow;
import com.mapvendor.module.order.domain.BusinessType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppMapQueryService {
    private static final TypeReference<List<GeoPoint>> GEO_POINTS = new TypeReference<List<GeoPoint>>() { };
    private final AppMapQueryMapper mapper;
    private final ObjectMapper objectMapper;

    public AppMapQueryService(AppMapQueryMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public MapOverview getOverview(BusinessType type) {
        return new MapOverview(toUniversities(mapper.selectVisibleUniversities()),
                toBusinesses(mapper.selectVisibleBusinesses(type == null ? null : type.name())));
    }

    private List<UniversityMapItem> toUniversities(List<UniversityMapRow> rows) {
        Map<Long, UniversityAccumulator> grouped = new LinkedHashMap<Long, UniversityAccumulator>();
        for (UniversityMapRow row : rows) {
            UniversityAccumulator item = grouped.get(row.getId());
            if (item == null) {
                item = new UniversityAccumulator(row, parsePolygon(row.getPolygonJson()));
                grouped.put(row.getId(), item);
            }
            if (row.getImageUrl() != null) {
                item.imageUrls.add(row.getImageUrl());
            }
        }
        List<UniversityMapItem> result = new ArrayList<UniversityMapItem>();
        for (UniversityAccumulator item : grouped.values()) {
            result.add(new UniversityMapItem(item.row.getId(), item.row.getName(), item.row.getIntro(),
                    item.polygonPoints, item.imageUrls));
        }
        return result;
    }

    private List<BusinessMapItem> toBusinesses(List<BusinessMapRow> rows) {
        List<BusinessMapItem> result = new ArrayList<BusinessMapItem>();
        for (BusinessMapRow row : rows) {
            result.add(new BusinessMapItem(row.getId(), row.getName(), BusinessType.valueOf(row.getBusinessType()),
                    row.getAddress(), row.getLongitude(), row.getLatitude(), row.getIntro(), row.getCoverImageUrl()));
        }
        return result;
    }

    private List<GeoPoint> parsePolygon(String polygonJson) {
        try {
            return objectMapper.readValue(polygonJson, GEO_POINTS);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid university polygon data", ex);
        }
    }

    private static final class UniversityAccumulator {
        private final UniversityMapRow row;
        private final List<GeoPoint> polygonPoints;
        private final List<String> imageUrls = new ArrayList<String>();

        private UniversityAccumulator(UniversityMapRow row, List<GeoPoint> polygonPoints) {
            this.row = row;
            this.polygonPoints = polygonPoints;
        }
    }
}
