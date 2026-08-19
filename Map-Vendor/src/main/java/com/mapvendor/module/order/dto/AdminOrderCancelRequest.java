package com.mapvendor.module.order.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AdminOrderCancelRequest {
    @NotBlank
    @Size(max = 500)
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
