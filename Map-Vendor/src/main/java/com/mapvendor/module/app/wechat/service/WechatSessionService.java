package com.mapvendor.module.app.wechat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.integration.wechat.WechatProperties;
import com.mapvendor.integration.wechat.WechatSessionResponse;
import com.mapvendor.module.app.wechat.dto.WechatIdentityView;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WechatSessionService {
    private static final Logger LOG = LoggerFactory.getLogger(WechatSessionService.class);
    private final RestTemplate restTemplate;
    private final WechatProperties properties;
    private final ObjectMapper objectMapper;

    public WechatSessionService(RestTemplate wechatRestTemplate, WechatProperties properties,
                                ObjectMapper objectMapper) {
        this.restTemplate = wechatRestTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public WechatIdentityView exchange(String code) {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new BusinessException("WECHAT_NOT_CONFIGURED", "微信身份服务未配置", HttpStatus.SERVICE_UNAVAILABLE);
        }
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.getSessionUrl())
                .queryParam("appid", properties.getAppId())
                .queryParam("secret", properties.getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .encode()
                .toUri();
        final WechatSessionResponse response;
        try {
            String responseBody = restTemplate.getForObject(uri, String.class);
            if (!StringUtils.hasText(responseBody)) {
                LOG.warn("wechat_session_response_invalid reason=empty_body");
                throw new BusinessException("WECHAT_UPSTREAM_ERROR", "微信身份服务响应异常",
                        HttpStatus.BAD_GATEWAY);
            }
            response = objectMapper.readValue(responseBody, WechatSessionResponse.class);
        } catch (RestClientException ex) {
            LOG.warn("wechat_session_exchange_failed exceptionType={} rootCauseType={}",
                    ex.getClass().getSimpleName(), rootCauseType(ex));
            throw new BusinessException("WECHAT_UPSTREAM_ERROR", "微信身份服务暂时不可用", HttpStatus.BAD_GATEWAY);
        } catch (JsonProcessingException ex) {
            LOG.warn("wechat_session_response_invalid exceptionType={}", ex.getClass().getSimpleName());
            throw new BusinessException("WECHAT_UPSTREAM_ERROR", "微信身份服务响应异常", HttpStatus.BAD_GATEWAY);
        }
        if (response == null || response.getErrcode() != null || !StringUtils.hasText(response.getOpenid())) {
            throw new BusinessException("WECHAT_CODE_INVALID", "微信临时登录凭证无效或已过期", HttpStatus.BAD_REQUEST);
        }
        return new WechatIdentityView(response.getOpenid());
    }

    private String rootCauseType(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getClass().getSimpleName();
    }
}
