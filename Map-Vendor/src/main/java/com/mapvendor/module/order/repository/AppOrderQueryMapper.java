package com.mapvendor.module.order.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppOrderQueryMapper {
    long countByOpenid(@Param("openid") String openid);
    List<AdminOrderRow> selectPageByOpenid(@Param("openid") String openid,
            @Param("offset") long offset, @Param("pageSize") int pageSize);
    AdminOrderRow selectByIdAndOpenid(@Param("id") long id, @Param("openid") String openid);
    AdminOrderRow selectById(@Param("id") long id);
    int cancelPendingByUser(@Param("id") long id, @Param("openid") String openid);
    int insertUserCancelLog(@Param("orderId") long orderId, @Param("requestId") String requestId);
}
