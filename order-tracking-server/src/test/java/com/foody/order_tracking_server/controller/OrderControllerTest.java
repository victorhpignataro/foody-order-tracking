package com.foody.order_tracking_server.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.foody.order_tracking_server.dto.order.OrderCreateDto;
import com.foody.order_tracking_server.dto.order.OrderDto;
import com.foody.order_tracking_server.dto.order.OrderStatusUpdateDto;
import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;
import com.foody.order_tracking_server.model.OrderStatusEnum;
import com.foody.order_tracking_server.service.JwtService;
import com.foody.order_tracking_server.service.OrderService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto sampleOrderDto;
    private OrderCreateDto sampleCreateDto;
    private OrderItemDto sampleItemDto;

    @BeforeEach
    void setUp() {
        sampleItemDto = new OrderItemDto(1L, "X-Burger", new BigDecimal("25.00"), 2, 1L);
        sampleOrderDto = new OrderDto(1L, "João Silva", "Rua A, 123", OrderStatusEnum.RECEIVED,
                LocalDateTime.of(2024, 1, 15, 10, 30), List.of(sampleItemDto));
        sampleCreateDto = new OrderCreateDto("João Silva", "Rua A, 123",
                List.of(new OrderItemDto(null, "X-Burger", new BigDecimal("25.00"), 2, null)));
    }

    @Test
    void findAll_ShouldReturnPagedOrders() throws Exception {
        Page<OrderDto> page = new PageImpl<>(List.of(sampleOrderDto), PageRequest.of(0, 20), 1);
        when(orderService.search(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].client").value("João Silva"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findAll_WithSearchParameter_ShouldPassSearchToService() throws Exception {
        Page<OrderDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(orderService.search(eq("123"), any())).thenReturn(page);

        mockMvc.perform(get("/api/order").param("search", "123"))
                .andExpect(status().isOk());

        verify(orderService).search(eq("123"), any());
    }

    @Test
    void findById_WhenOrderExists_ShouldReturnOrder() throws Exception {
        when(orderService.findById(1L)).thenReturn(sampleOrderDto);

        mockMvc.perform(get("/api/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.client").value("João Silva"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void findById_WhenOrderNotExists_ShouldReturn404() throws Exception {
        when(orderService.findById(999L)).thenThrow(
                new com.foody.order_tracking_server.exception.OrderNotFoundException(999L));

        mockMvc.perform(get("/api/order/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido não encontrado: 999"));
    }

    @Test
    void create_WithValidData_ShouldCreateOrderAndReturn201() throws Exception {
        OrderDto createdDto = new OrderDto(1L, "João Silva", "Rua A, 123", OrderStatusEnum.RECEIVED,
                LocalDateTime.now(), List.of());
        when(orderService.createOrder(any(OrderCreateDto.class))).thenReturn(createdDto);

        mockMvc.perform(post("/api/order")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/order/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.client").value("João Silva"));
    }

    @Test
    void create_WithInvalidData_ShouldReturn400() throws Exception {
        OrderCreateDto invalidDto = new OrderCreateDto("", "", List.of());

        mockMvc.perform(post("/api/order")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithoutItems_ShouldReturn400() throws Exception {
        OrderCreateDto withoutItems = new OrderCreateDto("João Silva", "Rua A, 123", List.of());

        mockMvc.perform(post("/api/order")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withoutItems)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(orderService);
    }

    @Test
    void changeStatus_WithValidData_ShouldUpdateStatusAndReturn200() throws Exception {
        OrderDto updatedDto = new OrderDto(1L, "João Silva", "Rua A, 123", OrderStatusEnum.IN_PREPARATION,
                LocalDateTime.of(2024, 1, 15, 10, 30), List.of());
        when(orderService.changeStatus(eq(1L), eq(OrderStatusEnum.IN_PREPARATION))).thenReturn(updatedDto);

        OrderStatusUpdateDto statusUpdate = new OrderStatusUpdateDto(OrderStatusEnum.IN_PREPARATION);

        mockMvc.perform(patch("/api/order/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));
    }

    @Test
    void changeStatus_WithInvalidStatus_ShouldReturn400() throws Exception {
        OrderStatusUpdateDto statusUpdate = new OrderStatusUpdateDto(null);

        mockMvc.perform(patch("/api/order/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeStatus_WhenOrderNotExists_ShouldReturn404() throws Exception {
        when(orderService.changeStatus(eq(999L), eq(OrderStatusEnum.IN_PREPARATION)))
                .thenThrow(new com.foody.order_tracking_server.exception.OrderNotFoundException(999L));

        OrderStatusUpdateDto statusUpdate = new OrderStatusUpdateDto(OrderStatusEnum.IN_PREPARATION);

        mockMvc.perform(patch("/api/order/999/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido não encontrado: 999"));
    }
}