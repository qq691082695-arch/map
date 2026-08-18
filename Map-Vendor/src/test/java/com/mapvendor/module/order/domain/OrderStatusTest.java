package com.mapvendor.module.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {
    @Test
    void pendingCanReachOnlyTerminalStates() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    void terminalStatesCannotTransition() {
        for (OrderStatus target : OrderStatus.values()) {
            assertThat(OrderStatus.CONFIRMED.canTransitionTo(target)).isFalse();
            assertThat(OrderStatus.CANCELLED.canTransitionTo(target)).isFalse();
        }
    }
}
