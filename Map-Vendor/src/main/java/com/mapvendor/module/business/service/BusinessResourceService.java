package com.mapvendor.module.business.service;

import com.mapvendor.common.api.*;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.business.domain.*;
import com.mapvendor.module.business.dto.*;
import com.mapvendor.module.business.repository.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BusinessResourceService {
    private static final Logger AUDIT=LoggerFactory.getLogger("AUDIT"); private final BusinessResourceMapper mapper;
    public BusinessResourceService(BusinessResourceMapper mapper){this.mapper=mapper;}
    @Transactional(readOnly=true)
    public PageResult<BusinessResourceView> list(BusinessResourceType type,long businessId,PageQuery page,ResourceStatus status){
        requireBusiness(type,businessId); String s=status==null?null:status.name(); long total=mapper.count(type.getTable(),businessId,s);
        List<BusinessResourceView> items=new ArrayList<BusinessResourceView>();
        for(BusinessResourceRow row:mapper.selectPage(type.getTable(),businessId,s,((long)page.getPage()-1L)*page.getPageSize(),page.getPageSize()))items.add(view(row));
        return new PageResult<BusinessResourceView>(items,total,page.getPage(),page.getPageSize());
    }
    @Transactional(readOnly=true) public BusinessResourceView get(BusinessResourceType type,long businessId,long id){requireBusiness(type,businessId);return view(requireResource(type,businessId,id));}
    @Transactional public BusinessResourceView createCar(long bid,CarSaveRequest r){requireBusiness(BusinessResourceType.CARS,bid);return create(BusinessResourceType.CARS,car(bid,r));}
    @Transactional public BusinessResourceView updateCar(long bid,long id,CarSaveRequest r){requireBusiness(BusinessResourceType.CARS,bid);BusinessResourceRow row=car(bid,r);row.setId(id);return update(BusinessResourceType.CARS,row);}
    @Transactional public BusinessResourceView createRoom(long bid,RoomSaveRequest r){requireBusiness(BusinessResourceType.ROOMS,bid);return create(BusinessResourceType.ROOMS,room(bid,r));}
    @Transactional public BusinessResourceView updateRoom(long bid,long id,RoomSaveRequest r){requireBusiness(BusinessResourceType.ROOMS,bid);BusinessResourceRow row=room(bid,r);row.setId(id);return update(BusinessResourceType.ROOMS,row);}
    @Transactional public BusinessResourceView createDish(long bid,DishSaveRequest r){requireBusiness(BusinessResourceType.DISHES,bid);return create(BusinessResourceType.DISHES,dish(bid,r));}
    @Transactional public BusinessResourceView updateDish(long bid,long id,DishSaveRequest r){requireBusiness(BusinessResourceType.DISHES,bid);BusinessResourceRow row=dish(bid,r);row.setId(id);return update(BusinessResourceType.DISHES,row);}
    @Transactional public BusinessResourceView status(BusinessResourceType t,long bid,long id,ResourceStatus s){requireBusiness(t,bid);changed(mapper.updateStatus(t.getTable(),bid,id,s.name()));audit(t,"STATUS_"+s,id,bid);return view(requireResource(t,bid,id));}
    @Transactional public void delete(BusinessResourceType t,long bid,long id){requireBusiness(t,bid);changed(mapper.softDelete(t.getTable(),bid,id));audit(t,"DELETE",id,bid);}
    private BusinessResourceView create(BusinessResourceType t,BusinessResourceRow row){row.setStatus(ResourceStatus.ENABLED.name());mapper.insert(t.getTable(),row);audit(t,"CREATE",row.getId(),row.getBusinessId());return view(requireResource(t,row.getBusinessId(),row.getId()));}
    private BusinessResourceView update(BusinessResourceType t,BusinessResourceRow row){changed(mapper.update(t.getTable(),row));audit(t,"UPDATE",row.getId(),row.getBusinessId());return view(requireResource(t,row.getBusinessId(),row.getId()));}
    private void requireBusiness(BusinessResourceType t,long id){String actual=mapper.selectBusinessType(id);if(actual==null)throw new BusinessException("BUSINESS_NOT_FOUND","服务商不存在",HttpStatus.NOT_FOUND);if(!actual.equals(t.getBusinessType().name()))throw new BusinessException("BUSINESS_TYPE_MISMATCH","附属资源与服务商类型不匹配",HttpStatus.BAD_REQUEST);}
    private BusinessResourceRow requireResource(BusinessResourceType t,long bid,long id){BusinessResourceRow r=mapper.selectOne(t.getTable(),bid,id);if(r==null)throw new BusinessException("RESOURCE_NOT_FOUND","附属资源不存在或不属于该服务商",HttpStatus.NOT_FOUND);return r;}
    private void changed(int n){if(n!=1)throw new BusinessException("RESOURCE_NOT_FOUND","附属资源不存在或不属于该服务商",HttpStatus.NOT_FOUND);}
    private BusinessResourceRow car(long b,CarSaveRequest r){BusinessResourceRow x=base(b,r.getModel(),r.getDescription(),r.getImageResourceId());x.setSeatNum(r.getSeatNum());return x;}
    private BusinessResourceRow room(long b,RoomSaveRequest r){BusinessResourceRow x=base(b,r.getName(),r.getDescription(),r.getImageResourceId());x.setBedSpec(r.getBedSpec().trim());return x;}
    private BusinessResourceRow dish(long b,DishSaveRequest r){BusinessResourceRow x=base(b,r.getName(),r.getDescription(),r.getImageResourceId());x.setSortNo(r.getSortNo());return x;}
    private BusinessResourceRow base(long b,String name,String desc,Long image){if(image!=null&&mapper.countActiveImage(image)!=1)throw new BusinessException("VALIDATION_ERROR","图片资源不存在、已禁用或已删除",HttpStatus.BAD_REQUEST);BusinessResourceRow x=new BusinessResourceRow();x.setBusinessId(b);x.setName(name.trim());x.setDescription(StringUtils.hasText(desc)?desc.trim():null);x.setImageResourceId(image);return x;}
    private BusinessResourceView view(BusinessResourceRow r){return new BusinessResourceView(r.getId(),r.getBusinessId(),r.getName(),r.getSeatNum(),r.getBedSpec(),r.getDescription(),r.getImageResourceId(),r.getSortNo(),ResourceStatus.valueOf(r.getStatus()),r.getCreatedAt(),r.getUpdatedAt());}
    private void audit(BusinessResourceType t,String action,long id,long bid){AUDIT.info("admin_business_resource_write type={} action={} businessId={} resourceId={} requestId={}",t.name(),action,bid,id,MDC.get("requestId"));}
}
