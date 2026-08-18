package com.mapvendor.module.university.controller;

import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.common.api.PageQuery;
import com.mapvendor.common.api.PageResult;
import com.mapvendor.module.university.domain.UniversityStatus;
import com.mapvendor.module.university.dto.UniversitySaveRequest;
import com.mapvendor.module.university.dto.UniversityStatusRequest;
import com.mapvendor.module.university.dto.UniversityView;
import com.mapvendor.module.university.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/universities")
@Tag(name = "AdminUniversity")
public class AdminUniversityController {
    private final UniversityService service;

    public AdminUniversityController(UniversityService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "分页查询高校")
    public ApiResponse<PageResult<UniversityView>> list(@Valid PageQuery page,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) UniversityStatus status) {
        return ApiResponse.success(service.list(page, keyword, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询高校详情")
    public ApiResponse<UniversityView> get(@PathVariable long id) { return ApiResponse.success(service.get(id)); }

    @PostMapping
    @Operation(summary = "新增高校")
    public ApiResponse<UniversityView> create(@Valid @RequestBody UniversitySaveRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑高校")
    public ApiResponse<UniversityView> update(@PathVariable long id,
            @Valid @RequestBody UniversitySaveRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "启用或禁用高校")
    public ApiResponse<UniversityView> updateStatus(@PathVariable long id,
            @Valid @RequestBody UniversityStatusRequest request) {
        return ApiResponse.success(service.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除高校")
    public ApiResponse<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
