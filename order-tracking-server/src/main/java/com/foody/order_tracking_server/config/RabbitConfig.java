package com.foody.order_tracking_server.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  public static final String ROUTING_KEY = "order.status.changed";
  public static final String EXCHANGE = "order.events";
  public static final String QUEUE = "order.status.changed";

  @Bean
  DirectExchange orderExchange() {
    return new DirectExchange(EXCHANGE);
  }

  @Bean
  Queue orderStatusQueue() {
    return QueueBuilder.durable(QUEUE).build();
  }

  @Bean
  Binding orderStatusBinding(Queue orderStatusQueue, DirectExchange orderExchange) {
    return BindingBuilder.bind(orderStatusQueue).to(orderExchange).with(ROUTING_KEY);
  }

  @Bean
  MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}