package com.mapvendor.module.order.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminOrderRow {
    private Long id;
    private String orderNo;
    private String openid;
    private Long businessId;
    private String businessNameSnapshot;
    private String businessType;
    private String serviceType;
    private String contactName;
    private String contactPhone;
    private Integer peopleNum;
    private LocalDate serviceDate;
    private String status;
    private Long carId;
    private String carSpecSnapshot;
    private Integer carQuantity;
    private String serviceMode;
    private Long roomId;
    private String roomSpecSnapshot;
    private Integer roomQuantity;
    private String mealPeriod;
    private String optionSnapshotJson;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancelSource;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getOpenid() { return openid; } public void setOpenid(String openid) { this.openid = openid; }
    public Long getBusinessId() { return businessId; } public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public String getBusinessNameSnapshot() { return businessNameSnapshot; } public void setBusinessNameSnapshot(String v) { this.businessNameSnapshot = v; }
    public String getBusinessType() { return businessType; } public void setBusinessType(String v) { this.businessType = v; }
    public String getServiceType() { return serviceType; } public void setServiceType(String v) { this.serviceType = v; }
    public String getContactName() { return contactName; } public void setContactName(String v) { this.contactName = v; }
    public String getContactPhone() { return contactPhone; } public void setContactPhone(String v) { this.contactPhone = v; }
    public Integer getPeopleNum() { return peopleNum; } public void setPeopleNum(Integer v) { this.peopleNum = v; }
    public LocalDate getServiceDate() { return serviceDate; } public void setServiceDate(LocalDate v) { this.serviceDate = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
    public Long getCarId() { return carId; } public void setCarId(Long v) { this.carId = v; }
    public String getCarSpecSnapshot() { return carSpecSnapshot; } public void setCarSpecSnapshot(String v) { this.carSpecSnapshot = v; }
    public Integer getCarQuantity() { return carQuantity; } public void setCarQuantity(Integer v) { this.carQuantity = v; }
    public String getServiceMode() { return serviceMode; } public void setServiceMode(String v) { this.serviceMode = v; }
    public Long getRoomId() { return roomId; } public void setRoomId(Long v) { this.roomId = v; }
    public String getRoomSpecSnapshot() { return roomSpecSnapshot; } public void setRoomSpecSnapshot(String v) { this.roomSpecSnapshot = v; }
    public Integer getRoomQuantity() { return roomQuantity; } public void setRoomQuantity(Integer v) { this.roomQuantity = v; }
    public String getMealPeriod() { return mealPeriod; } public void setMealPeriod(String v) { this.mealPeriod = v; }
    public String getOptionSnapshotJson() { return optionSnapshotJson; } public void setOptionSnapshotJson(String v) { this.optionSnapshotJson = v; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; } public void setConfirmedAt(LocalDateTime v) { this.confirmedAt = v; }
    public LocalDateTime getCancelledAt() { return cancelledAt; } public void setCancelledAt(LocalDateTime v) { this.cancelledAt = v; }
    public String getCancelSource() { return cancelSource; } public void setCancelSource(String v) { this.cancelSource = v; }
    public String getCancelReason() { return cancelReason; } public void setCancelReason(String v) { this.cancelReason = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
