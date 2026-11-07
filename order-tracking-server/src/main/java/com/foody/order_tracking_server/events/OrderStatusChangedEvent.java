package com.foody.order_tracking_server.events;

import java.time.Instant;

public record OrderStatusChangedEvent(
        Long orderId,
        String oldStatus,
        String newStatus,
        Instant occurredAt) {
}