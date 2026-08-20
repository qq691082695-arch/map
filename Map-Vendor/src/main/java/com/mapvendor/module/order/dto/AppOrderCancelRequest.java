package com.mapvendor.module.order.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AppOrderCancelRequest {
    @NotBlank
    @Size(max = 128)
    private String openid;

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
}
