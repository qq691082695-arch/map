package com.mapvendor.module.business.dto;

import javax.validation.constraints.*;

public class RoomSaveRequest {
    @NotBlank @Size(max=128) private String name;
    @NotBlank @Size(max=128) private String bedSpec;
    @Size(max=500) private String description;
    @Positive private Long imageResourceId;
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getBedSpec(){return bedSpec;} public void setBedSpec(String v){bedSpec=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public Long getImageResourceId(){return imageResourceId;} public void setImageResourceId(Long v){imageResourceId=v;}
}
