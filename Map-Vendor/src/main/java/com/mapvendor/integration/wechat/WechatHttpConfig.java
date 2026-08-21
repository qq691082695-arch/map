package com.mapvendor.integration.wechat;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WechatHttpConfig {
    @Bean
    public RestTemplate wechatRestTemplate(RestTemplateBuilder builder, WechatProperties properties) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()))
                .build();
    }
}
