package com.mapvendor.module.order.domain;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        return this == PENDING && (target == CONFIRMED || target == CANCELLED);
    }
}
