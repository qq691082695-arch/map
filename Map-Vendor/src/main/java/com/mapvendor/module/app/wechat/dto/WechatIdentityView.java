package com.mapvendor.module.app.wechat.dto;

public class WechatIdentityView {
    private final String openid;

    public WechatIdentityView(String openid) {
        this.openid = openid;
    }

    public String getOpenid() { return openid; }
}
