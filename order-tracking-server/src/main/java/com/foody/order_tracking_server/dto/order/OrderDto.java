package com.foody.order_tracking_server.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;
import com.foody.order_tracking_server.model.OrderStatusEnum;

public record OrderDto(
        Long id,
        String client,
        String address,
        OrderStatusEnum status,
        LocalDateTime createdAt,
        List<OrderItemDto> items) {
}