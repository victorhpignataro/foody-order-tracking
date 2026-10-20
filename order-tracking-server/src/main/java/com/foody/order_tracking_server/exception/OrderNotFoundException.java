package com.foody.order_tracking_server.exception;

public class OrderNotFoundException extends RuntimeException {
  public OrderNotFoundException(Long orderId) {
    super("Pedido não encontrado: " + orderId);
  }
}