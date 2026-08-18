package com.mapvendor.module.app.map.controller;

import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.module.app.map.dto.MapOverview;
import com.mapvendor.module.app.map.service.AppMapQueryService;
import com.mapvendor.module.order.domain.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/map-overview")
@Tag(name = "AppMap")
public class AppMapController {
    private final AppMapQueryService service;

    public AppMapController(AppMapQueryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "获取高校区域与商家地图聚合数据")
    public ApiResponse<MapOverview> getOverview(@RequestParam(value = "type", required = false) BusinessType type) {
        return ApiResponse.success(service.getOverview(type));
    }
}
