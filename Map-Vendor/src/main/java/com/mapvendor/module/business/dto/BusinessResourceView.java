package com.mapvendor.module.business.dto;

import com.mapvendor.module.business.domain.ResourceStatus;
import java.time.LocalDateTime;

public class BusinessResourceView {
    private final Long id; private final Long businessId; private final String name; private final Integer seatNum;
    private final String bedSpec; private final String description; private final Long imageResourceId; private final String imageUrl;
    private final Integer sortNo; private final ResourceStatus status; private final LocalDateTime createdAt; private final LocalDateTime updatedAt;
    public BusinessResourceView(Long id, Long businessId, String name, Integer seatNum, String bedSpec, String description,
            Long imageResourceId, String imageUrl, Integer sortNo, ResourceStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id=id;this.businessId=businessId;this.name=name;this.seatNum=seatNum;this.bedSpec=bedSpec;this.description=description;
        this.imageResourceId=imageResourceId;this.imageUrl=imageUrl;this.sortNo=sortNo;this.status=status;this.createdAt=createdAt;this.updatedAt=updatedAt;
    }
    public Long getId(){return id;} public Long getBusinessId(){return businessId;} public String getName(){return name;}
    public Integer getSeatNum(){return seatNum;} public String getBedSpec(){return bedSpec;} public String getDescription(){return description;}
    public Long getImageResourceId(){return imageResourceId;} public String getImageUrl(){return imageUrl;} public Integer getSortNo(){return sortNo;} public ResourceStatus getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
