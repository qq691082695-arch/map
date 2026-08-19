package com.mapvendor.module.statistics.service;

import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.domain.BusinessType;
import com.mapvendor.module.statistics.dto.*;
import com.mapvendor.module.statistics.repository.AdminStatisticsMapper;
import com.mapvendor.module.statistics.repository.StatisticsRow;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminStatisticsService {
    private final AdminStatisticsMapper mapper;
    public AdminStatisticsService(AdminStatisticsMapper mapper) { this.mapper = mapper; }

    @Transactional(readOnly = true)
    public StatisticsOverviewView overview(LocalDate from, LocalDate to, BusinessType type, Long businessId) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("VALIDATION_ERROR", "服务日期起始值不能晚于结束值", HttpStatus.BAD_REQUEST);
        }
        String typeValue = type == null ? null : type.name();
        StatisticsRow totalRow = mapper.selectTotal(from, to, typeValue, businessId);
        StatusCounts total = counts(totalRow);
        List<BusinessStatisticsView> businesses = new ArrayList<BusinessStatisticsView>();
        for (StatisticsRow row : mapper.selectByBusiness(from, to, typeValue, businessId)) {
            businesses.add(new BusinessStatisticsView(row.getBusinessId(), row.getBusinessNameSnapshot(),
                    BusinessType.valueOf(row.getBusinessType()), row.getPendingCount(), row.getConfirmedCount(),
                    row.getCancelledCount(), row.getTotalCount()));
        }
        return new StatisticsOverviewView(from, to, total, businesses);
    }

    private StatusCounts counts(StatisticsRow row) {
        return row == null ? new StatusCounts(0, 0, 0, 0)
                : new StatusCounts(row.getPendingCount(), row.getConfirmedCount(), row.getCancelledCount(), row.getTotalCount());
    }
}
