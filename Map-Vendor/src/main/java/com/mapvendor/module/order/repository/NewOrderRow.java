package com.mapvendor.module.order.repository;
import com.mapvendor.module.order.dto.AppOrderCreateRequest;
public class NewOrderRow {
 private Long id; private Long userId; private String orderNo; private String businessName; private String businessType; private String carSnapshot; private String roomSnapshot; private AppOrderCreateRequest request;
 public Long getId(){return id;} public void setId(Long v){id=v;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
 public String getOrderNo(){return orderNo;} public void setOrderNo(String v){orderNo=v;} public String getBusinessName(){return businessName;} public void setBusinessName(String v){businessName=v;}
 public String getBusinessType(){return businessType;} public void setBusinessType(String v){businessType=v;} public String getCarSnapshot(){return carSnapshot;} public void setCarSnapshot(String v){carSnapshot=v;}
 public String getRoomSnapshot(){return roomSnapshot;} public void setRoomSnapshot(String v){roomSnapshot=v;} public AppOrderCreateRequest getRequest(){return request;} public void setRequest(AppOrderCreateRequest v){request=v;}
}
