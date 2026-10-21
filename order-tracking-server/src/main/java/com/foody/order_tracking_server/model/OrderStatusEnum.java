package com.foody.order_tracking_server.model;

import java.util.List;
import java.util.Map;

public enum OrderStatusEnum {
  RECEIVED,
  IN_PREPARATION,
  OUT_FOR_DELIVERY,
  DELIVERED,
  CANCELED;

  private static final Map<OrderStatusEnum, List<OrderStatusEnum>> ALLOWED_TRANSITIONS = Map.of(
      RECEIVED, List.of(IN_PREPARATION, CANCELED),
      IN_PREPARATION, List.of(OUT_FOR_DELIVERY, CANCELED),
      OUT_FOR_DELIVERY, List.of(DELIVERED, CANCELED),
      DELIVERED, List.of(),
      CANCELED, List.of());

  public boolean canTransitionTo(OrderStatusEnum target) {
    return target != null && ALLOWED_TRANSITIONS.getOrDefault(this, List.of()).contains(target);
  }

  public List<OrderStatusEnum> getAllowedTransitions() {
    return ALLOWED_TRANSITIONS.getOrDefault(this, List.of());
  }
}
