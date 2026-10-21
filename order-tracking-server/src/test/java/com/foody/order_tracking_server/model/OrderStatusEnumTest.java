package com.foody.order_tracking_server.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusEnumTest {

  @Test
  void received_ShouldAllowPreparationAndCancelOnly() {
    assertThat(OrderStatusEnum.RECEIVED.canTransitionTo(OrderStatusEnum.IN_PREPARATION)).isTrue();
    assertThat(OrderStatusEnum.RECEIVED.canTransitionTo(OrderStatusEnum.CANCELED)).isTrue();
    assertThat(OrderStatusEnum.RECEIVED.canTransitionTo(OrderStatusEnum.OUT_FOR_DELIVERY)).isFalse();
    assertThat(OrderStatusEnum.RECEIVED.canTransitionTo(OrderStatusEnum.DELIVERED)).isFalse();
    assertThat(OrderStatusEnum.RECEIVED.canTransitionTo(OrderStatusEnum.RECEIVED)).isFalse();
    assertThat(OrderStatusEnum.RECEIVED.canTransitionTo(null)).isFalse();
  }

  @Test
  void inPreparation_ShouldAllowDeliveryAndCancelOnly() {
    assertThat(OrderStatusEnum.IN_PREPARATION.canTransitionTo(OrderStatusEnum.OUT_FOR_DELIVERY)).isTrue();
    assertThat(OrderStatusEnum.IN_PREPARATION.canTransitionTo(OrderStatusEnum.CANCELED)).isTrue();
    assertThat(OrderStatusEnum.IN_PREPARATION.canTransitionTo(OrderStatusEnum.DELIVERED)).isFalse();
    assertThat(OrderStatusEnum.IN_PREPARATION.canTransitionTo(OrderStatusEnum.RECEIVED)).isFalse();
  }

  @Test
  void outForDelivery_ShouldAllowDeliveredAndCancelOnly() {
    assertThat(OrderStatusEnum.OUT_FOR_DELIVERY.canTransitionTo(OrderStatusEnum.DELIVERED)).isTrue();
    assertThat(OrderStatusEnum.OUT_FOR_DELIVERY.canTransitionTo(OrderStatusEnum.CANCELED)).isTrue();
    assertThat(OrderStatusEnum.OUT_FOR_DELIVERY.canTransitionTo(OrderStatusEnum.IN_PREPARATION)).isFalse();
  }

  @Test
  void terminalStatuses_ShouldNotAllowAnyTransition() {
    assertThat(OrderStatusEnum.DELIVERED.getAllowedTransitions()).isEmpty();
    assertThat(OrderStatusEnum.CANCELED.getAllowedTransitions()).isEmpty();
    assertThat(OrderStatusEnum.DELIVERED.canTransitionTo(OrderStatusEnum.CANCELED)).isFalse();
    assertThat(OrderStatusEnum.CANCELED.canTransitionTo(OrderStatusEnum.RECEIVED)).isFalse();
  }
}
