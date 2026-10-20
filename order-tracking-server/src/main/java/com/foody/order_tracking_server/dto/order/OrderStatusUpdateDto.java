package com.foody.order_tracking_server.dto.order;

import com.foody.order_tracking_server.model.OrderStatusEnum;

import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateDto(@NotNull OrderStatusEnum status) {
}