package com.mapvendor.module.app.wechat.controller;

import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.module.app.wechat.dto.WechatCodeRequest;
import com.mapvendor.module.app.wechat.dto.WechatIdentityView;
import com.mapvendor.module.app.wechat.service.WechatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/wechat")
@Tag(name = "AppWechatIdentity")
public class WechatSessionController {
    private final WechatSessionService service;

    public WechatSessionController(WechatSessionService service) {
        this.service = service;
    }

    @PostMapping("/session")
    @Operation(summary = "使用小程序临时 code 静默换取 openid")
    public ApiResponse<WechatIdentityView> session(@Valid @RequestBody WechatCodeRequest request) {
        return ApiResponse.success(service.exchange(request.getCode()));
    }
}
