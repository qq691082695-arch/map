package com.mapvendor.module.order.dto;

import com.mapvendor.module.order.domain.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppOrderView {
    private final long id; private final String orderNo; private final long businessId; private final String businessNameSnapshot;
    private final BusinessType serviceType; private final String contactName; private final String contactPhone; private final int peopleNum;
    private final LocalDate serviceDate; private final OrderStatus status; private final Long carId; private final String carSpecSnapshot;
    private final Integer carQuantity; private final ServiceMode serviceMode; private final Long roomId; private final String roomSpecSnapshot;
    private final Integer roomQuantity; private final MealPeriod mealPeriod; private final LocalDateTime confirmedAt; private final LocalDateTime cancelledAt;
    private final CancelSource cancelSource; private final String cancelReason; private final LocalDateTime createdAt; private final LocalDateTime updatedAt;
    public AppOrderView(long id,String orderNo,long businessId,String businessNameSnapshot,BusinessType serviceType,String contactName,
      String contactPhone,int peopleNum,LocalDate serviceDate,OrderStatus status,Long carId,String carSpecSnapshot,Integer carQuantity,
      ServiceMode serviceMode,Long roomId,String roomSpecSnapshot,Integer roomQuantity,MealPeriod mealPeriod,LocalDateTime confirmedAt,
      LocalDateTime cancelledAt,CancelSource cancelSource,String cancelReason,LocalDateTime createdAt,LocalDateTime updatedAt){
      this.id=id;this.orderNo=orderNo;this.businessId=businessId;this.businessNameSnapshot=businessNameSnapshot;this.serviceType=serviceType;
      this.contactName=contactName;this.contactPhone=contactPhone;this.peopleNum=peopleNum;this.serviceDate=serviceDate;this.status=status;
      this.carId=carId;this.carSpecSnapshot=carSpecSnapshot;this.carQuantity=carQuantity;this.serviceMode=serviceMode;this.roomId=roomId;
      this.roomSpecSnapshot=roomSpecSnapshot;this.roomQuantity=roomQuantity;this.mealPeriod=mealPeriod;this.confirmedAt=confirmedAt;
      this.cancelledAt=cancelledAt;this.cancelSource=cancelSource;this.cancelReason=cancelReason;this.createdAt=createdAt;this.updatedAt=updatedAt;
    }
    public long getId(){return id;} public String getOrderNo(){return orderNo;} public long getBusinessId(){return businessId;}
    public String getBusinessNameSnapshot(){return businessNameSnapshot;} public BusinessType getServiceType(){return serviceType;}
    public String getContactName(){return contactName;} public String getContactPhone(){return contactPhone;} public int getPeopleNum(){return peopleNum;}
    public LocalDate getServiceDate(){return serviceDate;} public OrderStatus getStatus(){return status;} public Long getCarId(){return carId;}
    public String getCarSpecSnapshot(){return carSpecSnapshot;} public Integer getCarQuantity(){return carQuantity;} public ServiceMode getServiceMode(){return serviceMode;}
    public Long getRoomId(){return roomId;} public String getRoomSpecSnapshot(){return roomSpecSnapshot;} public Integer getRoomQuantity(){return roomQuantity;}
    public MealPeriod getMealPeriod(){return mealPeriod;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
    public LocalDateTime getConfirmedAt(){return confirmedAt;} public LocalDateTime getCancelledAt(){return cancelledAt;}
    public CancelSource getCancelSource(){return cancelSource;} public String getCancelReason(){return cancelReason;}
}
