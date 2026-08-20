package com.mapvendor.module.order.service;

import com.mapvendor.common.api.PageQuery;
import com.mapvendor.common.api.PageResult;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.domain.*;
import com.mapvendor.module.order.dto.AppOrderView;
import com.mapvendor.module.order.repository.AdminOrderRow;
import com.mapvendor.module.order.repository.AppOrderQueryMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppOrderQueryService {
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private final AppOrderQueryMapper mapper;

    public AppOrderQueryService(AppOrderQueryMapper mapper) { this.mapper = mapper; }

    @Transactional(readOnly = true)
    public PageResult<AppOrderView> list(PageQuery page, String openid) {
        String owner = normalize(openid);
        long total = mapper.countByOpenid(owner);
        List<AppOrderView> items = new ArrayList<AppOrderView>();
        for (AdminOrderRow row : mapper.selectPageByOpenid(owner,
                ((long) page.getPage() - 1L) * page.getPageSize(), page.getPageSize())) {
            items.add(toView(row));
        }
        return new PageResult<AppOrderView>(items, total, page.getPage(), page.getPageSize());
    }

    @Transactional(readOnly = true)
    public AppOrderView get(long id, String openid) {
        AdminOrderRow row = mapper.selectByIdAndOpenid(id, normalize(openid));
        if (row == null) throw notFound();
        return toView(row);
    }

    @Transactional
    public AppOrderView cancel(long id, String openid) {
        String owner = normalize(openid);
        if (mapper.cancelPendingByUser(id, owner) != 1) throwCancelFailure(id, owner);
        if (mapper.insertUserCancelLog(id, MDC.get("requestId")) != 1) {
            throw new IllegalStateException("Failed to persist user cancellation log");
        }
        AUDIT.info("app_order_status_write orderId={} from=PENDING to=CANCELLED operatorType=USER requestId={}",
                id, MDC.get("requestId"));
        return toView(mapper.selectByIdAndOpenid(id, owner));
    }

    private void throwCancelFailure(long id, String openid) {
        AdminOrderRow owned = mapper.selectByIdAndOpenid(id, openid);
        if (owned == null) throw notFound();
        throw new BusinessException("ORDER_STATUS_CONFLICT", "只有待确认订单可以取消", HttpStatus.CONFLICT);
    }

    private BusinessException notFound() {
        return new BusinessException("ORDER_NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND);
    }

    private String normalize(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.length() == 0 || normalized.length() > 128) {
            throw new BusinessException("VALIDATION_ERROR", "openid 长度必须为 1-128", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private AppOrderView toView(AdminOrderRow r) {
        return new AppOrderView(r.getId(), r.getOrderNo(), r.getBusinessId(), r.getBusinessNameSnapshot(),
                BusinessType.valueOf(r.getServiceType()), r.getContactName(), maskPhone(r.getContactPhone()),
                r.getPeopleNum(), r.getServiceDate(), OrderStatus.valueOf(r.getStatus()), r.getCarId(),
                r.getCarSpecSnapshot(), r.getCarQuantity(), value(ServiceMode.class, r.getServiceMode()),
                r.getRoomId(), r.getRoomSpecSnapshot(), r.getRoomQuantity(), value(MealPeriod.class, r.getMealPeriod()),
                r.getConfirmedAt(), r.getCancelledAt(), value(CancelSource.class, r.getCancelSource()),
                r.getCancelReason(), r.getCreatedAt(), r.getUpdatedAt());
    }

    private String maskPhone(String value) {
        if (value == null || value.length() < 7) return "****";
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }
    private <E extends Enum<E>> E value(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }
}
