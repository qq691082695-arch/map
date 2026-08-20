package com.mapvendor.module.order.repository;

import com.mapvendor.module.order.dto.AppOrderCreateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppOrderCreateMapper {
    int claimIdempotency(@Param("openid") String openid,@Param("key") String key,@Param("hash") String hash,
            @Param("expiresAt") java.time.LocalDateTime expiresAt);
    IdempotencyRow lockIdempotency(@Param("openid") String openid,@Param("key") String key);
    int upsertUser(@Param("openid") String openid);
    Long selectUserId(@Param("openid") String openid);
    OrderBusinessRow selectVisibleBusiness(@Param("id") long id);
    OrderResourceRow selectVisibleCar(@Param("id") long id);
    OrderResourceRow selectVisibleRoom(@Param("id") long id);
    int insertOrder(@Param("row") NewOrderRow row);
    int insertInitialLog(@Param("orderId") long orderId,@Param("requestId") String requestId);
    int completeIdempotency(@Param("id") long id,@Param("orderId") long orderId);
    AdminOrderRow selectOrder(@Param("id") long id);
}
