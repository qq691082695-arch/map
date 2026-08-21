package com.mapvendor.integration.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WechatSessionResponse {
    private String openid;
    @JsonProperty("session_key")
    private String sessionKey;
    private String unionid;
    private Integer errcode;
    private String errmsg;

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getSessionKey() { return sessionKey; }
    public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }
    public String getUnionid() { return unionid; }
    public void setUnionid(String unionid) { this.unionid = unionid; }
    public Integer getErrcode() { return errcode; }
    public void setErrcode(Integer errcode) { this.errcode = errcode; }
    public String getErrmsg() { return errmsg; }
    public void setErrmsg(String errmsg) { this.errmsg = errmsg; }
}
