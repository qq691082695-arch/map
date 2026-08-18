package com.mapvendor.module.business.repository;

import java.util.List;
import com.mapvendor.module.university.repository.UniversityImageRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BusinessMapper {
    long count(@Param("keyword") String keyword, @Param("businessType") String businessType, @Param("status") String status);
    List<BusinessRow> selectPage(@Param("keyword") String keyword, @Param("businessType") String businessType,
            @Param("status") String status, @Param("offset") long offset, @Param("pageSize") int pageSize);
    BusinessRow selectById(@Param("id") long id);
    int insert(BusinessRow row);
    int update(BusinessRow row);
    int updateStatus(@Param("id") long id, @Param("status") String status);
    int softDelete(@Param("id") long id);
    List<UniversityImageRow> selectImages(@Param("businessId") long businessId);
    int countActiveResources(@Param("resourceIds") List<Long> resourceIds);
    int deleteImageRelations(@Param("businessId") long businessId);
    int insertImageRelation(@Param("businessId") long businessId, @Param("resourceId") long resourceId,
            @Param("sortNo") int sortNo);
}
