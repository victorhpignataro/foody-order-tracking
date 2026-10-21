package com.foody.order_tracking_server.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;
import com.foody.order_tracking_server.model.Order;
import com.foody.order_tracking_server.model.OrderItem;

@SpringBootTest
@ActiveProfiles("test")
class OrderItemMapperTest {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Test
    void entityToResponse_ShouldMapCorrectly() {
        Order order = new Order();
        order.setId(1L);

        OrderItem item = new OrderItem("X-Burger", 2, new BigDecimal("25.00"));
        item.setId(10L);
        item.setOrder(order);

        OrderItemDto dto = orderItemMapper.entityToResponse(item);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo("X-Burger");
        assertThat(dto.quantity()).isEqualTo(2);
        assertThat(dto.unitPrice()).isEqualByComparingTo("25.00");
        assertThat(dto.orderId()).isEqualTo(1L);
    }

    @Test
    void entityToResponse_WithNullOrder_ShouldSetOrderIdNull() {
        OrderItem item = new OrderItem("X-Burger", 2, new BigDecimal("25.00"));
        item.setId(10L);
        item.setOrder(null);

        OrderItemDto dto = orderItemMapper.entityToResponse(item);

        assertThat(dto.orderId()).isNull();
    }

    @Test
    void orderItemResponseToEntity_ShouldMapCorrectly() {
        OrderItemDto dto = new OrderItemDto(10L, "X-Burger", new BigDecimal("25.00"), 2, 1L);

        OrderItem entity = orderItemMapper.orderItemResponseToEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // ignored
        assertThat(entity.getName()).isEqualTo("X-Burger");
        assertThat(entity.getQuantity()).isEqualTo(2);
        assertThat(entity.getUnitPrice()).isEqualByComparingTo("25.00");
        assertThat(entity.getOrder()).isNull(); // ignored
    }
}