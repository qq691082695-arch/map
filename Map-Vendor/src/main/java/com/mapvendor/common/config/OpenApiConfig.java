package com.mapvendor.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI mapVendorOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Map Vendor API")
                .version("v1")
                .description("平台管理端和微信小程序预约 API。小程序通过临时 code 静默换取 openid；不建立 Session 或 JWT。"
                        + " /api/v1/admin/** 必须由 Nginx 网络限制、HTTPS、限流和审计保护。"
                        + " 订单接口仍以 openid 辨别归属，不构成完整授权体系。"));
    }

    @Bean
    public GroupedOpenApi appApi() {
        return GroupedOpenApi.builder().group("app").pathsToMatch("/api/v1/app/**").build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder().group("admin").pathsToMatch("/api/v1/admin/**").build();
    }

}
