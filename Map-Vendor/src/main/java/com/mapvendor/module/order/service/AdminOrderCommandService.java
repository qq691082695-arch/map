package com.mapvendor.module.order.service;

import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.domain.OrderStatus;
import com.mapvendor.module.order.dto.AdminOrderDetailView;
import com.mapvendor.module.order.repository.AdminOrderMapper;
import com.mapvendor.module.order.repository.AdminOrderRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminOrderCommandService {
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private final AdminOrderMapper mapper;
    private final AdminOrderQueryService queryService;

    public AdminOrderCommandService(AdminOrderMapper mapper, AdminOrderQueryService queryService) {
        this.mapper = mapper;
        this.queryService = queryService;
    }

    @Transactional
    public AdminOrderDetailView confirm(long id) {
        if (mapper.confirmPending(id) != 1) throwTransitionFailure(id);
        writeLog(id, OrderStatus.CONFIRMED, null);
        audit(id, OrderStatus.CONFIRMED);
        return queryService.get(id);
    }

    @Transactional
    public AdminOrderDetailView cancel(long id, String reason) {
        String normalizedReason = reason == null ? null : reason.trim();
        if (!StringUtils.hasText(normalizedReason)) {
            throw new BusinessException("VALIDATION_ERROR", "管理员取消原因不能为空", HttpStatus.BAD_REQUEST);
        }
        if (mapper.cancelPendingByAdmin(id, normalizedReason) != 1) throwTransitionFailure(id);
        writeLog(id, OrderStatus.CANCELLED, normalizedReason);
        audit(id, OrderStatus.CANCELLED);
        return queryService.get(id);
    }

    private void writeLog(long id, OrderStatus target, String reason) {
        if (mapper.insertStatusLog(id, target.name(), "ADMIN", reason, MDC.get("requestId")) != 1) {
            throw new IllegalStateException("Failed to persist order status log");
        }
    }

    private void audit(long id, OrderStatus target) {
        AUDIT.info("admin_order_status_write orderId={} from=PENDING to={} operatorType=ADMIN requestId={}",
                id, target.name(), MDC.get("requestId"));
    }

    private void throwTransitionFailure(long id) {
        AdminOrderRow row = mapper.selectById(id);
        if (row == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND);
        }
        throw new BusinessException("ORDER_STATUS_CONFLICT",
                "订单已被处理，只有待确认订单可以执行该操作", HttpStatus.CONFLICT);
    }
}
