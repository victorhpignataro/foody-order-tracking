package com.foody.order_tracking_server.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.foody.order_tracking_server.dto.order.OrderCreateDto;
import com.foody.order_tracking_server.dto.order.OrderDto;
import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;
import com.foody.order_tracking_server.exception.OrderNotFoundException;
import com.foody.order_tracking_server.mappers.OrderMapper;
import com.foody.order_tracking_server.model.Order;
import com.foody.order_tracking_server.model.OrderItem;
import com.foody.order_tracking_server.model.OrderStatusEnum;
import com.foody.order_tracking_server.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;
    private OrderDto sampleOrderDto;
    private OrderCreateDto sampleCreateDto;
    private OrderItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new OrderItem("X-Burger", 2, new BigDecimal("25.00"));
        sampleItem.setId(1L);

        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setClient("João Silva");
        sampleOrder.setAddress("Rua A, 123");
        sampleOrder.setStatus(OrderStatusEnum.RECEIVED);
        sampleOrder.addItem(sampleItem);

        sampleOrderDto = new OrderDto(1L, "João Silva", "Rua A, 123", OrderStatusEnum.RECEIVED, null, List.of());

        sampleCreateDto = new OrderCreateDto("João Silva", "Rua A, 123",
                List.of(new OrderItemDto(null, "X-Burger", new BigDecimal("25.00"), 2, null)));
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));
        when(orderMapper.entityListToDtoList(List.of(sampleOrder))).thenReturn(List.of(sampleOrderDto));

        List<OrderDto> result = orderService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).client()).isEqualTo("João Silva");
        verify(orderRepository).findAll();
        verify(orderMapper).entityListToDtoList(List.of(sampleOrder));
    }

    @Test
    void findById_WhenOrderExists_ShouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.entityToDto(sampleOrder)).thenReturn(sampleOrderDto);

        OrderDto result = orderService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.client()).isEqualTo("João Silva");
        verify(orderRepository).findById(1L);
        verify(orderMapper).entityToDto(sampleOrder);
    }

    @Test
    void findById_WhenOrderNotExists_ShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("Pedido não encontrado: 999");

        verify(orderRepository).findById(999L);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void createOrder_ShouldCreateAndReturnOrder() {
        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setClient("João Silva");
        savedOrder.setAddress("Rua A, 123");
        savedOrder.setStatus(OrderStatusEnum.RECEIVED);
        savedOrder.addItem(sampleItem);

        when(orderMapper.createDtoToEntity(sampleCreateDto)).thenReturn(sampleOrder);
        when(orderRepository.save(sampleOrder)).thenReturn(savedOrder);
        when(orderMapper.entityToDto(savedOrder)).thenReturn(sampleOrderDto);

        OrderDto result = orderService.createOrder(sampleCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.client()).isEqualTo("João Silva");
        verify(orderMapper).createDtoToEntity(sampleCreateDto);
        verify(orderRepository).save(sampleOrder);
        verify(orderMapper).entityToDto(savedOrder);
    }

    @Test
    void changeStatus_WhenOrderExists_ShouldUpdateAndReturnOrder() {
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setClient("João Silva");
        updatedOrder.setAddress("Rua A, 123");
        updatedOrder.setStatus(OrderStatusEnum.IN_PREPARATION);
        updatedOrder.addItem(sampleItem);

        OrderDto updatedDto = new OrderDto(1L, "João Silva", "Rua A, 123", OrderStatusEnum.IN_PREPARATION, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.entityToDto(updatedOrder)).thenReturn(updatedDto);

        OrderDto result = orderService.changeStatus(1L, OrderStatusEnum.IN_PREPARATION);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(OrderStatusEnum.IN_PREPARATION);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).entityToDto(updatedOrder);
    }

    @Test
    void changeStatus_WithInvalidTransition_ShouldThrowAndNotPersist() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder)); // RECEIVED

        assertThatThrownBy(() -> orderService.changeStatus(1L, OrderStatusEnum.DELIVERED))
            .isInstanceOf(com.foody.order_tracking_server.exception.InvalidStatusTransitionException.class)
            .hasMessageContaining("RECEIVED")
            .hasMessageContaining("DELIVERED");

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(orderMapper);
    }

    @Test
    void changeStatus_FromTerminalStatus_ShouldThrow() {
        sampleOrder.setStatus(OrderStatusEnum.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.changeStatus(1L, OrderStatusEnum.CANCELED))
            .isInstanceOf(com.foody.order_tracking_server.exception.InvalidStatusTransitionException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void changeStatus_ToSameStatus_ShouldThrow() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.changeStatus(1L, OrderStatusEnum.RECEIVED))
            .isInstanceOf(com.foody.order_tracking_server.exception.InvalidStatusTransitionException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void changeStatus_WhenOrderNotExists_ShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.changeStatus(999L, OrderStatusEnum.IN_PREPARATION))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("Pedido não encontrado: 999");

        verify(orderRepository).findById(999L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void search_WithNullSearch_ShouldReturnAllOrdersPaged() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Order> orderPage = new PageImpl<>(List.of(sampleOrder), pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.entityToDto(sampleOrder)).thenReturn(sampleOrderDto);

        Page<OrderDto> result = orderService.search(null, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).client()).isEqualTo("João Silva");
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void search_WithBlankSearch_ShouldReturnAllOrdersPaged() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Order> orderPage = new PageImpl<>(List.of(sampleOrder), pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.entityToDto(sampleOrder)).thenReturn(sampleOrderDto);

        Page<OrderDto> result = orderService.search("   ", pageable);

        assertThat(result).hasSize(1);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void search_WithNonNumericSearch_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<OrderDto> result = orderService.search("abc", pageable);

        assertThat(result).isEmpty();
        verifyNoInteractions(orderRepository);
    }

    @Test
    void search_WithNumericSearch_WhenOrderExists_ShouldReturnOrder() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.entityToDto(sampleOrder)).thenReturn(sampleOrderDto);

        Page<OrderDto> result = orderService.search("1", pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    void search_WithNumericSearch_WhenOrderNotExists_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Page<OrderDto> result = orderService.search("999", pageable);

        assertThat(result).isEmpty();
        verify(orderRepository).findById(999L);
    }
}