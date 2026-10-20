package com.foody.order_tracking_server.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foody.order_tracking_server.dto.order.OrderCreateDto;
import com.foody.order_tracking_server.dto.order.OrderDto;
import com.foody.order_tracking_server.exception.OrderNotFoundException;
import com.foody.order_tracking_server.mappers.OrderMapper;
import com.foody.order_tracking_server.model.Order;
import com.foody.order_tracking_server.model.OrderStatusEnum;
import com.foody.order_tracking_server.repository.OrderRepository;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  public OrderService(OrderMapper orderMapper, OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
    this.orderMapper = orderMapper;
  }

  @Transactional(readOnly = true)
  public List<OrderDto> findAll() {
    return orderMapper.entityListToDtoList(orderRepository.findAll());
  }

  @Transactional(readOnly = true)
  public OrderDto findById(Long orderId) {
    Order entity = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    return orderMapper.entityToDto(entity);
  }

  @Transactional
  public OrderDto createOrder(OrderCreateDto createDto) {
    Order entity = orderMapper.createDtoToEntity(createDto);
    Order saved = orderRepository.save(entity);
    return orderMapper.entityToDto(saved);
  }

  @Transactional
  public OrderDto changeStatus(Long orderId, OrderStatusEnum newStatus) {
    Order entity = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    entity.setStatus(newStatus);
    Order saved = orderRepository.save(entity);

    return orderMapper.entityToDto(saved);
  }

  public Page<OrderDto> search(String search, Pageable pageable) {
    if (search == null || search.isBlank()) {
      return orderRepository.findAll(pageable).map(orderMapper::entityToDto);
    }

    String trimmed = search.trim();

    if (!trimmed.matches("\\d+")) {
      return Page.empty(pageable);
    }

    Optional<Order> orderOpt = orderRepository.findById(Long.valueOf(trimmed));

    if (orderOpt.isEmpty()) {
      return Page.empty(pageable);
    }

    OrderDto dto = orderMapper.entityToDto(orderOpt.get());
    return new PageImpl<>(List.of(dto), pageable, 1);
  }
}