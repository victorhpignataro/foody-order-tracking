package com.foody.order_tracking_server.dto.order;

import java.util.List;

import com.foody.order_tracking_server.model.OrderItem;

public record OrderCreateDto(String client, String address, List<OrderItem> items) {
}
