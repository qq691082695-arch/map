package com.mapvendor.module.statistics.repository;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminStatisticsMapper {
    StatisticsRow selectTotal(@Param("serviceDateFrom") LocalDate serviceDateFrom,
            @Param("serviceDateTo") LocalDate serviceDateTo, @Param("businessType") String businessType,
            @Param("businessId") Long businessId);

    List<StatisticsRow> selectByBusiness(@Param("serviceDateFrom") LocalDate serviceDateFrom,
            @Param("serviceDateTo") LocalDate serviceDateTo, @Param("businessType") String businessType,
            @Param("businessId") Long businessId);
}
