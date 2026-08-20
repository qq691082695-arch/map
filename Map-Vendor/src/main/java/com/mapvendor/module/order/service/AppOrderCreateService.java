package com.mapvendor.module.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.domain.*;
import com.mapvendor.module.order.dto.*;
import com.mapvendor.module.order.repository.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppOrderCreateService {
    private static final Logger AUDIT=LoggerFactory.getLogger("AUDIT");
    private final AppOrderCreateMapper mapper; private final ObjectMapper objectMapper;
    public AppOrderCreateService(AppOrderCreateMapper mapper,ObjectMapper objectMapper){this.mapper=mapper;this.objectMapper=objectMapper;}

    @Transactional
    public AppOrderView create(String key, AppOrderCreateRequest request) {
        normalize(request); validateTypeFields(request);
        String hash=hash(request); int claimed=mapper.claimIdempotency(request.getOpenid(),key,hash,java.time.LocalDateTime.now().plusDays(1));
        IdempotencyRow idem=mapper.lockIdempotency(request.getOpenid(),key);
        if(!hash.equals(idem.getRequestHash())) throw new BusinessException("IDEMPOTENCY_CONFLICT","幂等键已用于不同请求",HttpStatus.CONFLICT);
        if("SUCCEEDED".equals(idem.getStatus())) return view(mapper.selectOrder(Long.parseLong(idem.getResponseRef())));
        if(claimed!=1) throw new BusinessException("IDEMPOTENCY_IN_PROGRESS","相同请求正在处理中",HttpStatus.CONFLICT);

        OrderBusinessRow business=mapper.selectVisibleBusiness(request.getBusinessId());
        if(business==null) throw new BusinessException("BUSINESS_NOT_FOUND","服务商不存在或不可见",HttpStatus.NOT_FOUND);
        if(!"ENABLED".equals(business.getStatus())) throw new BusinessException("BUSINESS_DISABLED","服务商已禁用，不能创建预约",HttpStatus.CONFLICT);
        if(!request.getServiceType().name().equals(business.getBusinessType())) throw bad("服务类型与服务商类型不一致");

        String carSnapshot=null,roomSnapshot=null;
        if(request.getServiceType()==BusinessType.TRAVEL){
            OrderResourceRow car=mapper.selectVisibleCar(request.getCarId());
            if(car==null) throw new BusinessException("CAR_NOT_FOUND","车辆不存在或不可见",HttpStatus.NOT_FOUND);
            if(!request.getBusinessId().equals(car.getBusinessId())||!"ENABLED".equals(car.getStatus())) throw bad("车辆未启用或不属于该服务商");
            carSnapshot=json("model",car.getName(),"seatNum",car.getSeatNum());
        } else if(request.getServiceType()==BusinessType.HOTEL){
            OrderResourceRow room=mapper.selectVisibleRoom(request.getRoomId());
            if(room==null) throw new BusinessException("ROOM_NOT_FOUND","房型不存在或不可见",HttpStatus.NOT_FOUND);
            if(!request.getBusinessId().equals(room.getBusinessId())||!"ENABLED".equals(room.getStatus())) throw bad("房型未启用或不属于该服务商");
            roomSnapshot=json("name",room.getName(),"bedSpec",room.getBedSpec());
        }
        mapper.upsertUser(request.getOpenid());
        NewOrderRow row=new NewOrderRow(); row.setRequest(request); row.setUserId(mapper.selectUserId(request.getOpenid()));
        row.setOrderNo("MV"+UUID.randomUUID().toString().replace("-","").substring(0,24).toUpperCase());
        row.setBusinessName(business.getName());row.setBusinessType(business.getBusinessType());row.setCarSnapshot(carSnapshot);row.setRoomSnapshot(roomSnapshot);
        if(mapper.insertOrder(row)!=1||mapper.insertInitialLog(row.getId(),MDC.get("requestId"))!=1||mapper.completeIdempotency(idem.getId(),row.getId())!=1)
            throw new IllegalStateException("Failed to persist created order atomically");
        AUDIT.info("app_order_create orderId={} businessId={} serviceType={} requestId={}",row.getId(),request.getBusinessId(),request.getServiceType(),MDC.get("requestId"));
        return view(mapper.selectOrder(row.getId()));
    }

    private void normalize(AppOrderCreateRequest r){r.setOpenid(r.getOpenid().trim());r.setContactName(r.getContactName().trim());r.setContactPhone(r.getContactPhone().trim());if(r.getServiceDate().isBefore(LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"))))throw bad("服务日期不能早于今天");}
    private void validateTypeFields(AppOrderCreateRequest r){
        boolean car=r.getCarId()!=null||r.getCarQuantity()!=null||r.getServiceMode()!=null, room=r.getRoomId()!=null||r.getRoomQuantity()!=null, meal=r.getMealPeriod()!=null;
        if(r.getServiceType()==BusinessType.TRAVEL && !(r.getCarId()!=null&&r.getCarQuantity()!=null&&r.getServiceMode()!=null&&!room&&!meal))throw bad("出行预约字段不完整或包含其他类型字段");
        if(r.getServiceType()==BusinessType.HOTEL && !(r.getRoomId()!=null&&r.getRoomQuantity()!=null&&!car&&!meal))throw bad("住宿预约字段不完整或包含其他类型字段");
        if(r.getServiceType()==BusinessType.FOOD && !(r.getMealPeriod()!=null&&!car&&!room))throw bad("餐饮预约字段不完整或包含其他类型字段");
    }
    private String hash(AppOrderCreateRequest r){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(objectMapper.writeValueAsBytes(r));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x&255));return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
    private String json(String k1,Object v1,String k2,Object v2){try{java.util.Map<String,Object> m=new java.util.LinkedHashMap<String,Object>();m.put(k1,v1);m.put(k2,v2);return objectMapper.writeValueAsString(m);}catch(JsonProcessingException e){throw new IllegalStateException(e);}}
    private AppOrderView view(AdminOrderRow r){return new AppOrderView(r.getId(),r.getOrderNo(),r.getBusinessId(),r.getBusinessNameSnapshot(),BusinessType.valueOf(r.getServiceType()),r.getContactName(),mask(r.getContactPhone()),r.getPeopleNum(),r.getServiceDate(),OrderStatus.valueOf(r.getStatus()),r.getCarId(),r.getCarSpecSnapshot(),r.getCarQuantity(),value(ServiceMode.class,r.getServiceMode()),r.getRoomId(),r.getRoomSpecSnapshot(),r.getRoomQuantity(),value(MealPeriod.class,r.getMealPeriod()),r.getConfirmedAt(),r.getCancelledAt(),value(CancelSource.class,r.getCancelSource()),r.getCancelReason(),r.getCreatedAt(),r.getUpdatedAt());}
    private String mask(String v){if(v.length()<7)return "****";return v.substring(0,3)+"****"+v.substring(v.length()-4);}
    private <E extends Enum<E>> E value(Class<E> c,String v){return v==null?null:Enum.valueOf(c,v);} private BusinessException bad(String m){return new BusinessException("VALIDATION_ERROR",m,HttpStatus.BAD_REQUEST);}
}
