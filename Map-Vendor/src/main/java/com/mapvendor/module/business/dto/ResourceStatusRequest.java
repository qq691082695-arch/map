package com.mapvendor.module.business.dto;

import com.mapvendor.module.business.domain.ResourceStatus;
import javax.validation.constraints.NotNull;

public class ResourceStatusRequest {
    @NotNull private ResourceStatus status;
    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }
}
