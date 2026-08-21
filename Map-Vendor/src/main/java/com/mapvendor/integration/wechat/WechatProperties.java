package com.mapvendor.integration.wechat;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "map-vendor.wechat")
public class WechatProperties {
    private String appId;
    private String appSecret;
    private String sessionUrl = "https://api.weixin.qq.com/sns/jscode2session";
    private int connectTimeoutMillis = 3000;
    private int readTimeoutMillis = 5000;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getSessionUrl() { return sessionUrl; }
    public void setSessionUrl(String sessionUrl) { this.sessionUrl = sessionUrl; }
    public int getConnectTimeoutMillis() { return connectTimeoutMillis; }
    public void setConnectTimeoutMillis(int connectTimeoutMillis) { this.connectTimeoutMillis = connectTimeoutMillis; }
    public int getReadTimeoutMillis() { return readTimeoutMillis; }
    public void setReadTimeoutMillis(int readTimeoutMillis) { this.readTimeoutMillis = readTimeoutMillis; }
}
