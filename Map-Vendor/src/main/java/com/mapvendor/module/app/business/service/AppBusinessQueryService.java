package com.mapvendor.module.app.business.service;

import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.app.business.dto.BusinessCommon;
import com.mapvendor.module.app.business.dto.BusinessDetail;
import com.mapvendor.module.app.business.dto.CarItem;
import com.mapvendor.module.app.business.dto.DishItem;
import com.mapvendor.module.app.business.dto.FoodDetail;
import com.mapvendor.module.app.business.dto.HotelDetail;
import com.mapvendor.module.app.business.dto.RoomItem;
import com.mapvendor.module.app.business.dto.TravelDetail;
import com.mapvendor.module.app.business.query.AppBusinessMapper;
import com.mapvendor.module.app.business.query.BusinessDetailRow;
import com.mapvendor.module.app.business.query.CarRow;
import com.mapvendor.module.app.business.query.DishRow;
import com.mapvendor.module.app.business.query.RoomRow;
import com.mapvendor.module.order.domain.BusinessType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppBusinessQueryService {
    private final AppBusinessMapper mapper;

    public AppBusinessQueryService(AppBusinessMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public BusinessDetail getDetail(long id) {
        BusinessDetailRow row = mapper.selectVisibleBusinessById(id);
        if (row == null) {
            throw new BusinessException("BUSINESS_NOT_FOUND", "服务商不存在或不可见", HttpStatus.NOT_FOUND);
        }
        BusinessType type = BusinessType.valueOf(row.getBusinessType());
        BusinessCommon common = new BusinessCommon(row.getId(), row.getName(), type, row.getAddress(),
                row.getLongitude(), row.getLatitude(), row.getIntro(), mapper.selectBusinessImageUrls(id));
        return new BusinessDetail(common, buildDetail(type, row));
    }

    private Object buildDetail(BusinessType type, BusinessDetailRow row) {
        if (type == BusinessType.TRAVEL) {
            List<CarItem> cars = new ArrayList<CarItem>();
            for (CarRow car : mapper.selectCars(row.getId())) {
                cars.add(new CarItem(car.getId(), car.getModel(), car.getSeatNum(),
                        car.getDescription(), car.getImageUrl()));
            }
            return new TravelDetail(BusinessType.TRAVEL, cars);
        }
        if (type == BusinessType.HOTEL) {
            List<RoomItem> rooms = new ArrayList<RoomItem>();
            for (RoomRow room : mapper.selectRooms(row.getId())) {
                rooms.add(new RoomItem(room.getId(), room.getName(), room.getBedSpec(),
                        room.getDescription(), room.getImageUrl()));
            }
            return new HotelDetail(BusinessType.HOTEL, rooms);
        }
        List<DishItem> dishes = new ArrayList<DishItem>();
        for (DishRow dish : mapper.selectDishes(row.getId())) {
            dishes.add(new DishItem(dish.getId(), dish.getName(), dish.getDescription(), dish.getImageUrl()));
        }
        return new FoodDetail(BusinessType.FOOD, row.getFoodContactName(), row.getFoodContactPhone(),
                row.getFoodRecommendedDishes(), dishes);
    }
}