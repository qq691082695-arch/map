package com.mapvendor.module.system.controller;

import com.mapvendor.common.api.ApiResponse;
import java.util.Collections;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/system")
public class AppSystemController {
    @GetMapping("/ping")
    public ApiResponse<Map<String, String>> ping() {
        return ApiResponse.success(Collections.singletonMap("status", "UP"));
    }
}
