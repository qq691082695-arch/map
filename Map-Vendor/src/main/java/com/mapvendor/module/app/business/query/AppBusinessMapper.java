package com.mapvendor.module.app.business.query;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppBusinessMapper {
    BusinessDetailRow selectVisibleBusinessById(@Param("id") long id);

    List<String> selectBusinessImageUrls(@Param("businessId") long businessId);

    List<CarRow> selectCars(@Param("businessId") long businessId);

    List<RoomRow> selectRooms(@Param("businessId") long businessId);

    List<DishRow> selectDishes(@Param("businessId") long businessId);
}