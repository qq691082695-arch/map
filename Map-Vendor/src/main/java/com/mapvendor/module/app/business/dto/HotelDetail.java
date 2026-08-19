package com.mapvendor.module.app.business.dto;

import com.mapvendor.module.order.domain.BusinessType;
import java.util.List;

public class HotelDetail {
    private final BusinessType kind;
    private final List<RoomItem> rooms;

    public HotelDetail(BusinessType kind, List<RoomItem> rooms) {
        this.kind = kind;
        this.rooms = rooms;
    }

    public BusinessType getKind() { return kind; }
    public List<RoomItem> getRooms() { return rooms; }
}