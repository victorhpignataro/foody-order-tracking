package com.foody.order_tracking_server.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foody.order_tracking_server.dto.order.OrderCreateDto;
import com.foody.order_tracking_server.dto.order.OrderDto;
import com.foody.order_tracking_server.dto.order.OrderStatusUpdateDto;
import com.foody.order_tracking_server.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/order")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public ResponseEntity<Page<OrderDto>> findAll(@RequestParam(required = false) String search,
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

    return ResponseEntity.ok(orderService.search(search, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.findById(id));
  }

  @PostMapping
  public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderCreateDto order) {
    OrderDto created = orderService.createOrder(order);
    URI location = URI.create("/api/order/" + created.id());
    return ResponseEntity.created(location).body(created);
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderDto> changeStatus(
      @PathVariable Long id,
      @Valid @RequestBody OrderStatusUpdateDto body) {
    return ResponseEntity.ok(orderService.changeStatus(id, body.status()));
  }
}