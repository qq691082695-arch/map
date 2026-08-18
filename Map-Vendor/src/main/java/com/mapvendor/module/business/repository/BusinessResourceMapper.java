package com.mapvendor.module.business.repository;

import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface BusinessResourceMapper {
    @Select("SELECT business_type FROM business WHERE id=#{id} AND deleted_at IS NULL") String selectBusinessType(long id);
    long count(@Param("table") String table,@Param("businessId") long businessId,@Param("status") String status);
    List<BusinessResourceRow> selectPage(@Param("table") String table,@Param("businessId") long businessId,@Param("status") String status,@Param("offset") long offset,@Param("pageSize") int pageSize);
    BusinessResourceRow selectOne(@Param("table") String table,@Param("businessId") long businessId,@Param("id") long id);
    int insert(@Param("table") String table,@Param("row") BusinessResourceRow row);
    int update(@Param("table") String table,@Param("row") BusinessResourceRow row);
    int updateStatus(@Param("table") String table,@Param("businessId") long businessId,@Param("id") long id,@Param("status") String status);
    int softDelete(@Param("table") String table,@Param("businessId") long businessId,@Param("id") long id);
    @Select("SELECT COUNT(*) FROM file_resource WHERE id=#{id} AND status='ACTIVE' AND deleted_at IS NULL")
    int countActiveImage(@Param("id") long id);
}
