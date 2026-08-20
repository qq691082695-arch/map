package com.mapvendor.module.order.controller;
import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.common.api.PageQuery;
import com.mapvendor.common.api.PageResult;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.dto.*;
import com.mapvendor.module.order.service.AppOrderCreateService;
import com.mapvendor.module.order.service.AppOrderQueryService;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController @RequestMapping("/api/v1/app/orders")
public class AppOrderController {
 private final AppOrderCreateService service; private final AppOrderQueryService queryService;
 public AppOrderController(AppOrderCreateService service,AppOrderQueryService queryService){this.service=service;this.queryService=queryService;}
 @PostMapping public ApiResponse<AppOrderView> create(@RequestHeader(value="Idempotency-Key",required=false) String key,@Valid @RequestBody AppOrderCreateRequest request){
   String normalized=key==null?null:key.trim(); if(!StringUtils.hasText(normalized)||normalized.length()>128)throw new BusinessException("VALIDATION_ERROR","Idempotency-Key 长度必须为 1-128",HttpStatus.BAD_REQUEST);
   return ApiResponse.success(service.create(normalized,request));
 }
 @GetMapping public ApiResponse<PageResult<AppOrderView>> list(@Valid PageQuery page,@RequestParam String openid){return ApiResponse.success(queryService.list(page,openid));}
 @GetMapping("/{id}") public ApiResponse<AppOrderView> get(@PathVariable @Min(1) long id,@RequestParam String openid){return ApiResponse.success(queryService.get(id,openid));}
 @PostMapping("/{id}/cancel") public ApiResponse<AppOrderView> cancel(@PathVariable @Min(1) long id,@Valid @RequestBody AppOrderCancelRequest request){return ApiResponse.success(queryService.cancel(id,request.getOpenid()));}
}
