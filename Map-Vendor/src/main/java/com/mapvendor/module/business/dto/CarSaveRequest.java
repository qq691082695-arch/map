package com.mapvendor.module.business.dto;

import javax.validation.constraints.*;

public class CarSaveRequest {
    @NotBlank @Size(max=128) private String model;
    @NotNull @Min(1) private Integer seatNum;
    @Size(max=500) private String description;
    @Positive private Long imageResourceId;
    public String getModel(){return model;} public void setModel(String v){model=v;}
    public Integer getSeatNum(){return seatNum;} public void setSeatNum(Integer v){seatNum=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public Long getImageResourceId(){return imageResourceId;} public void setImageResourceId(Long v){imageResourceId=v;}
}
