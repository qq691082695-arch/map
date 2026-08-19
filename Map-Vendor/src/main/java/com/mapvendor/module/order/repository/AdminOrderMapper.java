package com.mapvendor.module.order.repository;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminOrderMapper {
    long count(@Param("serviceDateFrom") LocalDate serviceDateFrom, @Param("serviceDateTo") LocalDate serviceDateTo,
            @Param("status") String status, @Param("businessType") String businessType,
            @Param("businessId") Long businessId);
    List<AdminOrderRow> selectPage(@Param("serviceDateFrom") LocalDate serviceDateFrom,
            @Param("serviceDateTo") LocalDate serviceDateTo, @Param("status") String status,
            @Param("businessType") String businessType, @Param("businessId") Long businessId,
            @Param("offset") long offset, @Param("pageSize") int pageSize);
    AdminOrderRow selectById(@Param("id") long id);
    int confirmPending(@Param("id") long id);
    int cancelPendingByAdmin(@Param("id") long id, @Param("reason") String reason);
    int insertStatusLog(@Param("orderId") long orderId, @Param("toStatus") String toStatus,
            @Param("operatorType") String operatorType, @Param("reason") String reason,
            @Param("requestId") String requestId);
}
