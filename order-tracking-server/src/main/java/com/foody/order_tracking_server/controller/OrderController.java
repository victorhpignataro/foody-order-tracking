package com.foody.order_tracking_server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foody.order_tracking_server.model.Order;
import com.foody.order_tracking_server.repository.OrderRepository;

@RestController
@RequestMapping("/api/order")
public class OrderController {
  OrderRepository orderRepository;

  public OrderController(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @GetMapping
  public ResponseEntity<List<Order>> findAll() {

    List<Order> orders = orderRepository.findAll();

    return ResponseEntity.ok(orders);
  }
}
