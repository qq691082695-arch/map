package com.mapvendor.module.business.repository;

import java.time.LocalDateTime;

public class BusinessResourceRow {
    private Long id,businessId,imageResourceId; private String name,bedSpec,description,status; private Integer seatNum,sortNo; private LocalDateTime createdAt,updatedAt;
    public Long getId(){return id;} public void setId(Long v){id=v;} public Long getBusinessId(){return businessId;} public void setBusinessId(Long v){businessId=v;}
    public Long getImageResourceId(){return imageResourceId;} public void setImageResourceId(Long v){imageResourceId=v;} public String getName(){return name;} public void setName(String v){name=v;}
    public String getBedSpec(){return bedSpec;} public void setBedSpec(String v){bedSpec=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;} public Integer getSeatNum(){return seatNum;} public void setSeatNum(Integer v){seatNum=v;}
    public Integer getSortNo(){return sortNo;} public void setSortNo(Integer v){sortNo=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
