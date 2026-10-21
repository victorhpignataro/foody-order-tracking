package com.foody.order_tracking_server.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.foody.order_tracking_server.dto.order.OrderCreateDto;
import com.foody.order_tracking_server.dto.order.OrderDto;
import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;
import com.foody.order_tracking_server.model.Order;
import com.foody.order_tracking_server.model.OrderItem;
import com.foody.order_tracking_server.model.OrderStatusEnum;

@SpringBootTest
@ActiveProfiles("test")
class OrderMapperTest {

    private static final OrderItemDto CREATE_ITEM = new OrderItemDto(null, "X-Burger", new BigDecimal("25.00"), 2, null);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Test
    void createDtoToEntity_ShouldMapCorrectly() {
        OrderCreateDto createDto = new OrderCreateDto("João Silva", "Rua A, 123", List.of(CREATE_ITEM));

        Order entity = orderMapper.createDtoToEntity(createDto);

        assertThat(entity).isNotNull();
        assertThat(entity.getClient()).isEqualTo("João Silva");
        assertThat(entity.getAddress()).isEqualTo("Rua A, 123");
        assertThat(entity.getStatus()).isEqualTo(OrderStatusEnum.RECEIVED); // default status
        assertThat(entity.getItems()).hasSize(1);
        assertThat(entity.getItems().get(0).getName()).isEqualTo("X-Burger");
        assertThat(entity.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(entity.getItems().get(0).getUnitPrice()).isEqualByComparingTo("25.00");
        // Verify bidirectional relationship
        assertThat(entity.getItems().get(0).getOrder()).isSameAs(entity);
    }

    @Test
    void createDtoToEntity_WithEmptyItems_ShouldCreateOrderWithEmptyItems() {
        OrderCreateDto createDto = new OrderCreateDto("João Silva", "Rua A, 123", List.of());

        Order entity = orderMapper.createDtoToEntity(createDto);

        assertThat(entity).isNotNull();
        assertThat(entity.getItems()).isEmpty();
    }

    @Test
    void entityToDto_ShouldMapCorrectly() {
        OrderItem item = new OrderItem("X-Burger", 2, new BigDecimal("25.00"));
        item.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setClient("João Silva");
        order.setAddress("Rua A, 123");
        order.setStatus(OrderStatusEnum.IN_PREPARATION);
        order.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        order.addItem(item);

        OrderDto dto = orderMapper.entityToDto(order);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.client()).isEqualTo("João Silva");
        assertThat(dto.address()).isEqualTo("Rua A, 123");
        assertThat(dto.status()).isEqualTo(OrderStatusEnum.IN_PREPARATION);
        assertThat(dto.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
        assertThat(dto.items()).hasSize(1);
        assertThat(dto.items().get(0).id()).isEqualTo(1L);
        assertThat(dto.items().get(0).name()).isEqualTo("X-Burger");
        assertThat(dto.items().get(0).quantity()).isEqualTo(2);
        assertThat(dto.items().get(0).unitPrice()).isEqualByComparingTo("25.00");
        assertThat(dto.items().get(0).orderId()).isEqualTo(1L);
    }

    @Test
    void entityListToDtoList_ShouldMapAllOrders() {
        OrderItem item1 = new OrderItem("X-Burger", 2, new BigDecimal("25.00"));
        item1.setId(1L);
        Order order1 = new Order();
        order1.setId(1L);
        order1.setClient("João Silva");
        order1.setAddress("Rua A, 123");
        order1.setStatus(OrderStatusEnum.RECEIVED);
        order1.addItem(item1);

        OrderItem item2 = new OrderItem("Refrigerante", 1, new BigDecimal("8.00"));
        item2.setId(2L);
        Order order2 = new Order();
        order2.setId(2L);
        order2.setClient("Maria Santos");
        order2.setAddress("Rua B, 456");
        order2.setStatus(OrderStatusEnum.DELIVERED);
        order2.addItem(item2);

        List<OrderDto> dtos = orderMapper.entityListToDtoList(List.of(order1, order2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).client()).isEqualTo("João Silva");
        assertThat(dtos.get(1).client()).isEqualTo("Maria Santos");
    }
}