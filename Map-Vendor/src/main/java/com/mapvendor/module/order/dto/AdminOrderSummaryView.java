package com.mapvendor.module.order.dto;

import com.mapvendor.module.order.domain.BusinessType;
import com.mapvendor.module.order.domain.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminOrderSummaryView {
    private final long id; private final String orderNo; private final String openidMasked;
    private final long businessId; private final String businessNameSnapshot; private final BusinessType serviceType;
    private final String contactName; private final String contactPhoneMasked; private final int peopleNum;
    private final LocalDate serviceDate; private final OrderStatus status; private final LocalDateTime createdAt;
    public AdminOrderSummaryView(long id,String orderNo,String openidMasked,long businessId,String businessNameSnapshot,
            BusinessType serviceType,String contactName,String contactPhoneMasked,int peopleNum,LocalDate serviceDate,
            OrderStatus status,LocalDateTime createdAt) {
        this.id=id;this.orderNo=orderNo;this.openidMasked=openidMasked;this.businessId=businessId;
        this.businessNameSnapshot=businessNameSnapshot;this.serviceType=serviceType;this.contactName=contactName;
        this.contactPhoneMasked=contactPhoneMasked;this.peopleNum=peopleNum;this.serviceDate=serviceDate;
        this.status=status;this.createdAt=createdAt;
    }
    public long getId(){return id;} public String getOrderNo(){return orderNo;} public String getOpenidMasked(){return openidMasked;}
    public long getBusinessId(){return businessId;} public String getBusinessNameSnapshot(){return businessNameSnapshot;}
    public BusinessType getServiceType(){return serviceType;} public String getContactName(){return contactName;}
    public String getContactPhoneMasked(){return contactPhoneMasked;} public int getPeopleNum(){return peopleNum;}
    public LocalDate getServiceDate(){return serviceDate;} public OrderStatus getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
