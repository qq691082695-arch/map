package com.mapvendor.module.business.dto;

import javax.validation.constraints.*;

public class DishSaveRequest {
    @NotBlank @Size(max=128) private String name;
    @Size(max=500) private String description;
    @Positive private Long imageResourceId;
    @NotNull @Min(0) private Integer sortNo;
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public Long getImageResourceId(){return imageResourceId;} public void setImageResourceId(Long v){imageResourceId=v;}
    public Integer getSortNo(){return sortNo;} public void setSortNo(Integer v){sortNo=v;}
}
