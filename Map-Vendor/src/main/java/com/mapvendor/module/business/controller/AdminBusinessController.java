package com.mapvendor.module.business.controller;

import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.common.api.PageQuery;
import com.mapvendor.common.api.PageResult;
import com.mapvendor.module.business.domain.BusinessStatus;
import com.mapvendor.module.business.dto.BusinessSaveRequest;
import com.mapvendor.module.business.dto.BusinessStatusRequest;
import com.mapvendor.module.business.dto.BusinessView;
import com.mapvendor.module.business.service.BusinessService;
import com.mapvendor.module.order.domain.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/admin/businesses")
@Tag(name = "AdminBusiness")
public class AdminBusinessController {
    private final BusinessService service;
    public AdminBusinessController(BusinessService service) { this.service = service; }
    @GetMapping @Operation(summary = "分页查询服务商")
    public ApiResponse<PageResult<BusinessView>> list(@Valid PageQuery page,
            @RequestParam(value="keyword", required=false) String keyword,
            @RequestParam(value="type", required=false) BusinessType type,
            @RequestParam(value="status", required=false) BusinessStatus status) {
        return ApiResponse.success(service.list(page, keyword, type, status));
    }
    @GetMapping("/{id}") @Operation(summary = "查询服务商详情")
    public ApiResponse<BusinessView> get(@PathVariable long id) { return ApiResponse.success(service.get(id)); }
    @PostMapping @Operation(summary = "新增服务商")
    public ApiResponse<BusinessView> create(@Valid @RequestBody BusinessSaveRequest request) { return ApiResponse.success(service.create(request)); }
    @PutMapping("/{id}") @Operation(summary = "编辑服务商，类型不可修改")
    public ApiResponse<BusinessView> update(@PathVariable long id, @Valid @RequestBody BusinessSaveRequest request) { return ApiResponse.success(service.update(id, request)); }
    @PatchMapping("/{id}/status") @Operation(summary = "启用或禁用服务商")
    public ApiResponse<BusinessView> status(@PathVariable long id, @Valid @RequestBody BusinessStatusRequest request) { return ApiResponse.success(service.updateStatus(id, request.getStatus())); }
    @DeleteMapping("/{id}") @Operation(summary = "逻辑删除服务商")
    public ApiResponse<Void> delete(@PathVariable long id) { service.delete(id); return ApiResponse.success(null); }
}
