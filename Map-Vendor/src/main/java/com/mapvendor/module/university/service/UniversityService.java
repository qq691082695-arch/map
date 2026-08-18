package com.mapvendor.module.university.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapvendor.common.api.PageQuery;
import com.mapvendor.common.api.PageResult;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.app.map.dto.GeoPoint;
import com.mapvendor.module.university.domain.UniversityStatus;
import com.mapvendor.module.university.dto.UniversityImage;
import com.mapvendor.module.university.dto.UniversitySaveRequest;
import com.mapvendor.module.university.dto.UniversityView;
import com.mapvendor.module.university.repository.UniversityImageRow;
import com.mapvendor.module.university.repository.UniversityMapper;
import com.mapvendor.module.university.repository.UniversityRow;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UniversityService {
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private static final TypeReference<List<GeoPoint>> POINTS = new TypeReference<List<GeoPoint>>() { };
    private final UniversityMapper mapper;
    private final ObjectMapper objectMapper;

    public UniversityService(UniversityMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<UniversityView> list(PageQuery page, String keyword, UniversityStatus status) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String statusValue = status == null ? null : status.name();
        long total = mapper.count(normalizedKeyword, statusValue);
        List<UniversityView> items = new ArrayList<UniversityView>();
        for (UniversityRow row : mapper.selectPage(normalizedKeyword, statusValue,
                ((long) page.getPage() - 1L) * page.getPageSize(), page.getPageSize())) {
            items.add(toView(row));
        }
        return new PageResult<UniversityView>(items, total, page.getPage(), page.getPageSize());
    }

    @Transactional(readOnly = true)
    public UniversityView get(long id) {
        return toView(required(id));
    }

    @Transactional
    public UniversityView create(UniversitySaveRequest request) {
        Prepared prepared = prepare(request);
        UniversityRow row = new UniversityRow();
        row.setName(request.getName().trim());
        row.setIntro(normalize(request.getIntro()));
        row.setPolygonJson(prepared.polygonJson);
        row.setStatus(UniversityStatus.ENABLED.name());
        mapper.insert(row);
        replaceImages(row.getId(), prepared.imageIds);
        audit("CREATE", row.getId());
        return toView(required(row.getId()));
    }

    @Transactional
    public UniversityView update(long id, UniversitySaveRequest request) {
        required(id);
        Prepared prepared = prepare(request);
        UniversityRow row = new UniversityRow();
        row.setId(id);
        row.setName(request.getName().trim());
        row.setIntro(normalize(request.getIntro()));
        row.setPolygonJson(prepared.polygonJson);
        mapper.update(row);
        replaceImages(id, prepared.imageIds);
        audit("UPDATE", id);
        return toView(required(id));
    }

    @Transactional
    public UniversityView updateStatus(long id, UniversityStatus status) {
        required(id);
        mapper.updateStatus(id, status.name());
        audit("STATUS_" + status.name(), id);
        return toView(required(id));
    }

    @Transactional
    public void delete(long id) {
        ensureChanged(mapper.softDelete(id), id);
        audit("DELETE", id);
    }

    private Prepared prepare(UniversitySaveRequest request) {
        validatePolygon(request.getPolygonPoints());
        List<Long> imageIds = request.getImageResourceIds() == null
                ? new ArrayList<Long>() : new ArrayList<Long>(request.getImageResourceIds());
        if (imageIds.contains(null) || new LinkedHashSet<Long>(imageIds).size() != imageIds.size()) {
            throw validation("图片资源 ID 不能为空或重复");
        }
        if (!imageIds.isEmpty() && mapper.countActiveResources(imageIds) != imageIds.size()) {
            throw validation("图片资源不存在、已禁用或已删除");
        }
        try {
            return new Prepared(objectMapper.writeValueAsString(request.getPolygonPoints()), imageIds);
        } catch (JsonProcessingException ex) {
            throw validation("高校多边形无法序列化");
        }
    }

    private void validatePolygon(List<GeoPoint> points) {
        if (points == null || points.size() < 3) {
            throw validation("高校多边形至少需要 3 个点");
        }
        Set<String> distinct = new HashSet<String>();
        for (GeoPoint point : points) {
            if (point == null || point.getLatitude() == null || point.getLongitude() == null
                    || point.getLatitude().doubleValue() < -90 || point.getLatitude().doubleValue() > 90
                    || point.getLongitude().doubleValue() < -180 || point.getLongitude().doubleValue() > 180) {
                throw validation("高校多边形经纬度不合法");
            }
            distinct.add(point.getLatitude().stripTrailingZeros().toPlainString() + ":"
                    + point.getLongitude().stripTrailingZeros().toPlainString());
        }
        if (distinct.size() < 3) {
            throw validation("高校多边形至少需要 3 个不同点");
        }
    }

    private UniversityRow required(long id) {
        UniversityRow row = mapper.selectById(id);
        if (row == null) {
            throw new BusinessException("UNIVERSITY_NOT_FOUND", "高校不存在", HttpStatus.NOT_FOUND);
        }
        return row;
    }

    private UniversityView toView(UniversityRow row) {
        List<GeoPoint> points;
        try {
            points = objectMapper.readValue(row.getPolygonJson(), POINTS);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid university polygon data: " + row.getId(), ex);
        }
        List<UniversityImage> images = new ArrayList<UniversityImage>();
        for (UniversityImageRow image : mapper.selectImages(row.getId())) {
            images.add(new UniversityImage(image.getResourceId(), image.getPublicUrl(), image.getSortNo()));
        }
        return new UniversityView(row.getId(), row.getName(), row.getIntro(), points,
                UniversityStatus.valueOf(row.getStatus()), images, row.getCreatedAt(), row.getUpdatedAt());
    }

    private void replaceImages(long id, List<Long> imageIds) {
        mapper.deleteImageRelations(id);
        for (int index = 0; index < imageIds.size(); index++) {
            mapper.insertImageRelation(id, imageIds.get(index), index);
        }
    }

    private void ensureChanged(int changed, long id) {
        if (changed != 1) {
            throw new BusinessException("UNIVERSITY_NOT_FOUND", "高校不存在", HttpStatus.NOT_FOUND);
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BusinessException validation(String message) {
        return new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    private void audit(String action, long id) {
        AUDIT.info("admin_university_write action={} universityId={} requestId={}", action, id, MDC.get("requestId"));
    }

    private static final class Prepared {
        private final String polygonJson;
        private final List<Long> imageIds;
        private Prepared(String polygonJson, List<Long> imageIds) {
            this.polygonJson = polygonJson;
            this.imageIds = imageIds;
        }
    }
}
