package com.mapvendor.module.university.dto;

import com.mapvendor.module.university.domain.UniversityStatus;
import javax.validation.constraints.NotNull;

public class UniversityStatusRequest {
    @NotNull
    private UniversityStatus status;

    public UniversityStatus getStatus() { return status; }
    public void setStatus(UniversityStatus status) { this.status = status; }
}
