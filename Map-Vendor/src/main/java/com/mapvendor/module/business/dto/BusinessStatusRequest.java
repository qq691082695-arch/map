package com.mapvendor.module.business.dto;

import com.mapvendor.module.business.domain.BusinessStatus;
import javax.validation.constraints.NotNull;

public class BusinessStatusRequest {
    @NotNull
    private BusinessStatus status;
    public BusinessStatus getStatus() { return status; }
    public void setStatus(BusinessStatus status) { this.status = status; }
}
