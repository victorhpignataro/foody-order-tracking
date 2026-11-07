package com.foody.order_tracking_server.events;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.foody.order_tracking_server.config.RabbitConfig;

@Component
public class OrderEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onStatusChanged(OrderStatusChangedEvent event) {
    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
  }
}