package com.foody.order_tracking_server.dto.orderitem;

import java.math.BigDecimal;

public record OrderItemDto(Long id,
    String name,
    BigDecimal unitPrice,
    Integer quantity,
    Long orderId) {

}
