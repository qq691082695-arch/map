package com.mapvendor.module.app.wechat.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class WechatCodeRequest {
    @NotBlank
    @Size(max = 256)
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
