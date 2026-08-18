package com.mapvendor.module.business.service;

import com.mapvendor.common.api.PageQuery;
import com.mapvendor.common.api.PageResult;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.business.domain.BusinessStatus;
import com.mapvendor.module.business.dto.BusinessSaveRequest;
import com.mapvendor.module.business.dto.BusinessView;
import com.mapvendor.module.business.repository.BusinessMapper;
import com.mapvendor.module.business.repository.BusinessRow;
import com.mapvendor.module.order.domain.BusinessType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import com.mapvendor.module.university.dto.UniversityImage;
import com.mapvendor.module.university.repository.UniversityImageRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BusinessService {
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private final BusinessMapper mapper;
    public BusinessService(BusinessMapper mapper) { this.mapper = mapper; }

    @Transactional(readOnly = true)
    public PageResult<BusinessView> list(PageQuery page, String keyword, BusinessType type, BusinessStatus status) {
        String normalized = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String typeValue = type == null ? null : type.name();
        String statusValue = status == null ? null : status.name();
        long total = mapper.count(normalized, typeValue, statusValue);
        List<BusinessView> items = new ArrayList<BusinessView>();
        for (BusinessRow row : mapper.selectPage(normalized, typeValue, statusValue,
                ((long) page.getPage() - 1L) * page.getPageSize(), page.getPageSize())) items.add(toView(row));
        return new PageResult<BusinessView>(items, total, page.getPage(), page.getPageSize());
    }

    @Transactional(readOnly = true)
    public BusinessView get(long id) { return toView(required(id)); }

    @Transactional
    public BusinessView create(BusinessSaveRequest request) {
        validateTypeFields(request);
        BusinessRow row = rowFrom(request);
        row.setStatus(BusinessStatus.ENABLED.name());
        mapper.insert(row); audit("CREATE", row.getId());
        replaceImages(row.getId(), validateImages(request.getImageResourceIds()));
        return toView(required(row.getId()));
    }

    @Transactional
    public BusinessView update(long id, BusinessSaveRequest request) {
        BusinessRow existing = required(id);
        if (!existing.getBusinessType().equals(request.getBusinessType().name()))
            throw validation("服务商类型创建后不可修改");
        validateTypeFields(request);
        BusinessRow row = rowFrom(request); row.setId(id);
        ensureChanged(mapper.update(row), id); audit("UPDATE", id);
        replaceImages(id, validateImages(request.getImageResourceIds()));
        return toView(required(id));
    }

    @Transactional
    public BusinessView updateStatus(long id, BusinessStatus status) {
        ensureChanged(mapper.updateStatus(id, status.name()), id); audit("STATUS_" + status.name(), id);
        return toView(required(id));
    }

    @Transactional
    public void delete(long id) { ensureChanged(mapper.softDelete(id), id); audit("DELETE", id); }

    private BusinessRow rowFrom(BusinessSaveRequest request) {
        BusinessRow row = new BusinessRow(); row.setName(request.getName().trim());
        row.setAddress(request.getAddress().trim()); row.setLongitude(request.getLongitude());
        row.setLatitude(request.getLatitude()); row.setBusinessType(request.getBusinessType().name());
        row.setIntro(normalize(request.getIntro()));
        if (request.getBusinessType() == BusinessType.FOOD) {
            row.setFoodContactName(normalize(request.getFoodContactName()));
            row.setFoodContactPhone(normalize(request.getFoodContactPhone()));
            row.setFoodRecommendedDishes(normalize(request.getFoodRecommendedDishes()));
        }
        return row;
    }

    private void validateTypeFields(BusinessSaveRequest request) {
        if (request.getBusinessType() != BusinessType.FOOD && (StringUtils.hasText(request.getFoodContactName())
                || StringUtils.hasText(request.getFoodContactPhone()) || StringUtils.hasText(request.getFoodRecommendedDishes())))
            throw validation("仅餐饮服务商可填写餐饮专属字段");
    }

    private BusinessRow required(long id) {
        BusinessRow row = mapper.selectById(id);
        if (row == null) throw notFound();
        return row;
    }
    private void ensureChanged(int changed, long id) { if (changed != 1) throw notFound(); }
    private BusinessException notFound() { return new BusinessException("BUSINESS_NOT_FOUND", "服务商不存在", HttpStatus.NOT_FOUND); }
    private BusinessException validation(String message) { return new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST); }
    private String normalize(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
    private List<Long> validateImages(List<Long> source) {
        List<Long> ids = source == null ? new ArrayList<Long>() : new ArrayList<Long>(source);
        if (ids.contains(null) || new LinkedHashSet<Long>(ids).size() != ids.size()) throw validation("图片资源 ID 不能为空或重复");
        if (!ids.isEmpty() && mapper.countActiveResources(ids) != ids.size()) throw validation("图片资源不存在、已禁用或已删除");
        return ids;
    }
    private void replaceImages(long businessId,List<Long> ids){mapper.deleteImageRelations(businessId);for(int i=0;i<ids.size();i++)mapper.insertImageRelation(businessId,ids.get(i),i);}
    private BusinessView toView(BusinessRow row) { List<UniversityImage> images=new ArrayList<UniversityImage>(); for(UniversityImageRow image:mapper.selectImages(row.getId()))images.add(new UniversityImage(image.getResourceId(),image.getPublicUrl(),image.getSortNo())); return new BusinessView(row.getId(), row.getName(), row.getAddress(),
            row.getLongitude(), row.getLatitude(), BusinessType.valueOf(row.getBusinessType()), row.getIntro(),
            row.getFoodContactName(), row.getFoodContactPhone(), row.getFoodRecommendedDishes(),
            BusinessStatus.valueOf(row.getStatus()), images, row.getCreatedAt(), row.getUpdatedAt()); }
    private void audit(String action, long id) { AUDIT.info("admin_business_write action={} businessId={} requestId={}", action, id, MDC.get("requestId")); }
}
