package com.foody.order_tracking_server.exception;

import com.foody.order_tracking_server.model.OrderStatusEnum;

public class InvalidStatusTransitionException extends RuntimeException {

  public InvalidStatusTransitionException(OrderStatusEnum current, OrderStatusEnum target) {
    super("Transição de status inválida: " + current + " -> " + target);
  }
}
