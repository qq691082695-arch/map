package com.mapvendor.module.university.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UniversityMapper {
    long count(@Param("keyword") String keyword, @Param("status") String status);
    List<UniversityRow> selectPage(@Param("keyword") String keyword, @Param("status") String status,
            @Param("offset") long offset, @Param("pageSize") int pageSize);
    UniversityRow selectById(@Param("id") long id);
    List<UniversityImageRow> selectImages(@Param("universityId") long universityId);
    int countActiveResources(@Param("resourceIds") List<Long> resourceIds);
    int insert(UniversityRow row);
    int update(UniversityRow row);
    int updateStatus(@Param("id") long id, @Param("status") String status);
    int softDelete(@Param("id") long id);
    int deleteImageRelations(@Param("universityId") long universityId);
    int insertImageRelation(@Param("universityId") long universityId,
            @Param("resourceId") long resourceId, @Param("sortNo") int sortNo);
}
