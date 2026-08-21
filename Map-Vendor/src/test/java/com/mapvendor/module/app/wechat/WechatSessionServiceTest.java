package com.mapvendor.module.app.wechat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.mapvendor.common.error.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapvendor.integration.wechat.WechatProperties;
import com.mapvendor.module.app.wechat.dto.WechatIdentityView;
import com.mapvendor.module.app.wechat.service.WechatSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class WechatSessionServiceTest {
    @Test
    void exchangesTemporaryCodeWithoutExposingSessionKey() {
        WechatProperties properties = configuredProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(queryParam("appid", "wx-test"))
                .andExpect(queryParam("secret", "server-only-secret"))
                .andExpect(queryParam("js_code", "temporary-code"))
                .andRespond(withSuccess("{\"openid\":\"openid-123\",\"session_key\":\"must-not-return\"}",
                        MediaType.APPLICATION_JSON));

        WechatIdentityView result = service(restTemplate, properties).exchange("temporary-code");

        assertThat(result.getOpenid()).isEqualTo("openid-123");
        server.verify();
    }

    @Test
    void rejectsMissingSecretWithoutCallingWechat() {
        WechatProperties properties = new WechatProperties();
        properties.setAppId("wx-test");
        assertThatThrownBy(() -> service(new RestTemplate(), properties).exchange("code"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo("WECHAT_NOT_CONFIGURED");
    }

    @Test
    void mapsWechatErrorToSafeClientError() {
        WechatProperties properties = configuredProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(queryParam("js_code", "expired"))
                .andRespond(withSuccess("{\"errcode\":40029,\"errmsg\":\"invalid code\"}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service(restTemplate, properties).exchange("expired"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("微信临时登录凭证无效或已过期")
                .extracting("code")
                .isEqualTo("WECHAT_CODE_INVALID");
        server.verify();
    }

    @Test
    void mapsEmptyWechatResponseToSafeUpstreamError() {
        WechatProperties properties = configuredProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(queryParam("js_code", "empty"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service(restTemplate, properties).exchange("empty"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("微信身份服务响应异常")
                .extracting("code")
                .isEqualTo("WECHAT_UPSTREAM_ERROR");
        server.verify();
    }

    private WechatProperties configuredProperties() {
        WechatProperties properties = new WechatProperties();
        properties.setAppId("wx-test");
        properties.setAppSecret("server-only-secret");
        properties.setSessionUrl("https://api.weixin.qq.com/sns/jscode2session");
        return properties;
    }

    private WechatSessionService service(RestTemplate restTemplate, WechatProperties properties) {
        return new WechatSessionService(restTemplate, properties, new ObjectMapper());
    }
}
