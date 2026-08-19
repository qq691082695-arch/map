package com.mapvendor.module.order.service;

import com.mapvendor.common.api.PageQuery;
import com.mapvendor.common.api.PageResult;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.domain.*;
import com.mapvendor.module.order.dto.AdminOrderDetailView;
import com.mapvendor.module.order.dto.AdminOrderSummaryView;
import com.mapvendor.module.order.repository.AdminOrderMapper;
import com.mapvendor.module.order.repository.AdminOrderRow;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrderQueryService {
    private final AdminOrderMapper mapper;
    public AdminOrderQueryService(AdminOrderMapper mapper) { this.mapper = mapper; }

    @Transactional(readOnly = true)
    public PageResult<AdminOrderSummaryView> list(PageQuery page, LocalDate from, LocalDate to,
            OrderStatus status, BusinessType type, Long businessId) {
        validateFilter(from, to);
        String statusValue = status == null ? null : status.name();
        String typeValue = type == null ? null : type.name();
        long total = mapper.count(from, to, statusValue, typeValue, businessId);
        List<AdminOrderSummaryView> items = new ArrayList<AdminOrderSummaryView>();
        for (AdminOrderRow row : mapper.selectPage(from, to, statusValue, typeValue, businessId,
                ((long) page.getPage() - 1L) * page.getPageSize(), page.getPageSize())) {
            items.add(toSummary(row));
        }
        return new PageResult<AdminOrderSummaryView>(items, total, page.getPage(), page.getPageSize());
    }

    public void validateFilter(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to))
            throw new BusinessException("VALIDATION_ERROR", "服务日期起始值不能晚于结束值", HttpStatus.BAD_REQUEST);
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailView get(long id) {
        AdminOrderRow row = mapper.selectById(id);
        if (row == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND);
        return new AdminOrderDetailView(row.getId(), row.getOrderNo(), maskOpenid(row.getOpenid()), row.getBusinessId(),
                row.getBusinessNameSnapshot(), BusinessType.valueOf(row.getBusinessType()), BusinessType.valueOf(row.getServiceType()),
                row.getContactName(), maskPhone(row.getContactPhone()), row.getContactPhone(), row.getPeopleNum(), row.getServiceDate(), OrderStatus.valueOf(row.getStatus()),
                row.getCarId(), row.getCarSpecSnapshot(), row.getCarQuantity(), enumValue(ServiceMode.class, row.getServiceMode()),
                row.getRoomId(), row.getRoomSpecSnapshot(), row.getRoomQuantity(), enumValue(MealPeriod.class, row.getMealPeriod()),
                row.getOptionSnapshotJson(), row.getConfirmedAt(), row.getCancelledAt(), enumValue(CancelSource.class, row.getCancelSource()),
                row.getCancelReason(), row.getCreatedAt(), row.getUpdatedAt());
    }

    private AdminOrderSummaryView toSummary(AdminOrderRow row) {
        return new AdminOrderSummaryView(row.getId(), row.getOrderNo(), maskOpenid(row.getOpenid()), row.getBusinessId(),
                row.getBusinessNameSnapshot(), BusinessType.valueOf(row.getServiceType()), row.getContactName(),
                maskPhone(row.getContactPhone()), row.getPeopleNum(), row.getServiceDate(),
                OrderStatus.valueOf(row.getStatus()), row.getCreatedAt());
    }
    private String maskPhone(String value) {
        if (value == null || value.length() < 7) return "****";
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }
    private String maskOpenid(String value) {
        if (value == null || value.length() <= 8) return "****";
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
    private <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }
}
