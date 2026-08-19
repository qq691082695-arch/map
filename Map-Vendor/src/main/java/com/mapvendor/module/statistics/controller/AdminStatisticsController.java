package com.mapvendor.module.statistics.controller;

import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.module.order.domain.BusinessType;
import com.mapvendor.module.statistics.dto.StatisticsOverviewView;
import com.mapvendor.module.statistics.service.AdminStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import javax.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/statistics")
@Tag(name = "AdminStatistics")
public class AdminStatisticsController {
    private final AdminStatisticsService service;
    public AdminStatisticsController(AdminStatisticsService service) { this.service = service; }

    @GetMapping("/overview")
    @Operation(summary = "按服务日期统计订单状态和服务商快照")
    public ApiResponse<StatisticsOverviewView> overview(
            @RequestParam(value="serviceDateFrom", required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value="serviceDateTo", required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value="type", required=false) BusinessType type,
            @RequestParam(value="businessId", required=false) @Min(1) Long businessId) {
        return ApiResponse.success(service.overview(from, to, type, businessId));
    }
}
