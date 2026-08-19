package com.mapvendor.module.order.controller;

import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.common.api.PageQuery;
import com.mapvendor.common.api.PageResult;
import com.mapvendor.module.order.domain.BusinessType;
import com.mapvendor.module.order.domain.OrderStatus;
import com.mapvendor.module.order.dto.AdminOrderDetailView;
import com.mapvendor.module.order.dto.AdminOrderCancelRequest;
import com.mapvendor.module.order.dto.AdminOrderSummaryView;
import com.mapvendor.module.order.service.AdminOrderCommandService;
import com.mapvendor.module.order.service.AdminOrderQueryService;
import com.mapvendor.integration.export.OrderExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@Validated
@RestController
@RequestMapping("/api/v1/admin/orders")
@Tag(name = "AdminOrder")
public class AdminOrderController {
    private final AdminOrderQueryService service;
    private final AdminOrderCommandService commandService;
    private final OrderExcelExportService exportService;
    public AdminOrderController(AdminOrderQueryService service, AdminOrderCommandService commandService,
            OrderExcelExportService exportService) {
        this.service = service;
        this.commandService = commandService;
        this.exportService = exportService;
    }

    @GetMapping("/export")
    @Operation(summary = "按订单列表筛选条件导出 Excel（最多 10000 行）")
    public void export(
            @RequestParam("serviceDateFrom") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("serviceDateTo") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value="status", required=false) OrderStatus status,
            @RequestParam(value="type", required=false) BusinessType type,
            @RequestParam(value="businessId", required=false) @Min(1) Long businessId,
            HttpServletResponse response) throws IOException {
        long total = exportService.validateAndCount(from, to, status, type, businessId);
        String filename = "orders-" + from + "-" + to + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" +
                URLEncoder.encode(filename, "UTF-8").replace("+", "%20"));
        exportService.export(from, to, status, type, businessId, total, response.getOutputStream());
    }

    @GetMapping
    @Operation(summary = "分页筛选全部订单")
    public ApiResponse<PageResult<AdminOrderSummaryView>> list(@Valid PageQuery page,
            @RequestParam(value="serviceDateFrom", required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value="serviceDateTo", required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value="status", required=false) OrderStatus status,
            @RequestParam(value="type", required=false) BusinessType type,
            @RequestParam(value="businessId", required=false) @Min(1) Long businessId) {
        return ApiResponse.success(service.list(page, from, to, status, type, businessId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询订单详情（使用历史快照）")
    public ApiResponse<AdminOrderDetailView> get(@PathVariable @Min(1) long id) {
        return ApiResponse.success(service.get(id));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "管理员确认待确认订单")
    public ApiResponse<AdminOrderDetailView> confirm(@PathVariable @Min(1) long id) {
        return ApiResponse.success(commandService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "管理员取消待确认订单")
    public ApiResponse<AdminOrderDetailView> cancel(@PathVariable @Min(1) long id,
            @Valid @RequestBody AdminOrderCancelRequest request) {
        return ApiResponse.success(commandService.cancel(id, request.getReason()));
    }
}
