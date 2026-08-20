package com.mapvendor.module.order.dto;

import com.mapvendor.module.order.domain.BusinessType;
import com.mapvendor.module.order.domain.MealPeriod;
import com.mapvendor.module.order.domain.ServiceMode;
import java.time.LocalDate;
import javax.validation.constraints.*;

public class AppOrderCreateRequest {
    @NotBlank @Size(max = 128) private String openid;
    @NotBlank @Size(max = 64) private String contactName;
    @NotBlank @Size(max = 32) private String contactPhone;
    @NotNull @Min(1) private Integer peopleNum;
    @NotNull private LocalDate serviceDate;
    @NotNull @Min(1) private Long businessId;
    @NotNull private BusinessType serviceType;
    @Min(1) private Long carId;
    @Min(1) private Integer carQuantity;
    private ServiceMode serviceMode;
    @Min(1) private Long roomId;
    @Min(1) private Integer roomQuantity;
    private MealPeriod mealPeriod;

    public String getOpenid(){return openid;} public void setOpenid(String v){openid=v;}
    public String getContactName(){return contactName;} public void setContactName(String v){contactName=v;}
    public String getContactPhone(){return contactPhone;} public void setContactPhone(String v){contactPhone=v;}
    public Integer getPeopleNum(){return peopleNum;} public void setPeopleNum(Integer v){peopleNum=v;}
    public LocalDate getServiceDate(){return serviceDate;} public void setServiceDate(LocalDate v){serviceDate=v;}
    public Long getBusinessId(){return businessId;} public void setBusinessId(Long v){businessId=v;}
    public BusinessType getServiceType(){return serviceType;} public void setServiceType(BusinessType v){serviceType=v;}
    public Long getCarId(){return carId;} public void setCarId(Long v){carId=v;}
    public Integer getCarQuantity(){return carQuantity;} public void setCarQuantity(Integer v){carQuantity=v;}
    public ServiceMode getServiceMode(){return serviceMode;} public void setServiceMode(ServiceMode v){serviceMode=v;}
    public Long getRoomId(){return roomId;} public void setRoomId(Long v){roomId=v;}
    public Integer getRoomQuantity(){return roomQuantity;} public void setRoomQuantity(Integer v){roomQuantity=v;}
    public MealPeriod getMealPeriod(){return mealPeriod;} public void setMealPeriod(MealPeriod v){mealPeriod=v;}
}
