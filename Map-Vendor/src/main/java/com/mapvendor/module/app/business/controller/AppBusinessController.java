package com.mapvendor.module.app.business.controller;

import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.module.app.business.dto.BusinessDetail;
import com.mapvendor.module.app.business.service.AppBusinessQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/businesses")
@Tag(name = "AppBusiness")
public class AppBusinessController {
    private final AppBusinessQueryService service;

    public AppBusinessController(AppBusinessQueryService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取服务商详情（含分类附属资源）")
    public ApiResponse<BusinessDetail> get(@PathVariable long id) {
        return ApiResponse.success(service.getDetail(id));
    }
}