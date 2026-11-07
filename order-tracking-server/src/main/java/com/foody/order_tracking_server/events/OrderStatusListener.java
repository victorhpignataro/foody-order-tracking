package com.foody.order_tracking_server.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.foody.order_tracking_server.config.RabbitConfig;

@Component
public class OrderStatusListener {

  private static final Logger log = LoggerFactory.getLogger(OrderStatusListener.class);

  @RabbitListener(queues = RabbitConfig.QUEUE, autoStartup = "${app.rabbit.listener.enabled:true}")
  public void onStatusChanged(OrderStatusChangedEvent event) {
    log.info("Pedido #{} mudou de {} para {}", event.orderId(), event.oldStatus(), event.newStatus());
  }
}