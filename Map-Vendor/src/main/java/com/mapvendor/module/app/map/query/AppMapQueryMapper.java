package com.mapvendor.module.app.map.query;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppMapQueryMapper {
    List<UniversityMapRow> selectVisibleUniversities();

    List<BusinessMapRow> selectVisibleBusinesses(@Param("type") String type);
}
