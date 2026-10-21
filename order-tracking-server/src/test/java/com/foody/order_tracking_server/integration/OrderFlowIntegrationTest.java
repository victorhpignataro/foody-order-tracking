package com.foody.order_tracking_server.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.foody.order_tracking_server.dto.order.OrderCreateDto;
import com.foody.order_tracking_server.dto.order.OrderDto;
import com.foody.order_tracking_server.dto.order.OrderStatusUpdateDto;
import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;
import com.foody.order_tracking_server.model.OrderStatusEnum;
import com.foody.order_tracking_server.security.JwtAuthFilter;
import com.foody.order_tracking_server.service.JwtService;

import jakarta.servlet.http.Cookie;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testes de integração dos pedidos com a cadeia de filtros habilitada.
 *
 * <p>{@link #authCookie()} gera um token válido, mas sem usuário cadastrado:
 * esses testes cobrem apenas a autenticação baseada em token. O fluxo completo
 * (cadastro, login e uso do cookie real) está em AuthFlowIntegrationTest.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullOrderFlow_CreateReadUpdate_ShouldWork() throws Exception {
        Cookie auth = authCookie();

        // 1. Create order
        OrderCreateDto createDto = new OrderCreateDto(
            "João Silva",
            "Rua A, 123",
            List.of(new OrderItemDto(null, "X-Burger", new BigDecimal("25.00"), 2, null))
        );

        String createResponse = mockMvc.perform(post("/api/order")
                .with(csrf())
                .cookie(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.client").value("João Silva"))
            .andExpect(jsonPath("$.status").value("RECEIVED"))
            .andExpect(jsonPath("$.items[0].name").value("X-Burger"))
            .andReturn().getResponse().getContentAsString();

        OrderDto createdOrder = objectMapper.readValue(createResponse, OrderDto.class);
        Long orderId = createdOrder.id();

        // 2. Read order by ID
        mockMvc.perform(get("/api/order/" + orderId).cookie(auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.client").value("João Silva"))
            .andExpect(jsonPath("$.items[0].name").value("X-Burger"));

        // 3. Update status
        OrderStatusUpdateDto statusUpdate = new OrderStatusUpdateDto(OrderStatusEnum.IN_PREPARATION);

        mockMvc.perform(patch("/api/order/" + orderId + "/status")
                .with(csrf())
                .cookie(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        // 4. List orders with search
        mockMvc.perform(get("/api/order").param("search", orderId.toString()).cookie(auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(orderId));
    }

    @Test
    void createOrder_WithoutItems_ShouldReturn400() throws Exception {
        OrderCreateDto createDto = new OrderCreateDto(
            "Maria Santos",
            "Rua B, 456",
            List.of()
        );

        mockMvc.perform(post("/api/order")
                .with(csrf())
                .cookie(authCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void changeStatus_ThroughAllStatuses_ShouldWork() throws Exception {
        Cookie auth = authCookie();
        Long orderId = createOrder(auth);

        // Test all status transitions
        OrderStatusEnum[] statuses = {
            OrderStatusEnum.IN_PREPARATION,
            OrderStatusEnum.OUT_FOR_DELIVERY,
            OrderStatusEnum.DELIVERED
        };

        for (OrderStatusEnum status : statuses) {
            OrderStatusUpdateDto statusUpdate = new OrderStatusUpdateDto(status);

            mockMvc.perform(patch("/api/order/" + orderId + "/status")
                    .with(csrf())
                    .cookie(auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status.name()));
        }
    }

    @Test
    void changeStatus_WithInvalidTransition_ShouldReturn409() throws Exception {
        Cookie auth = authCookie();
        Long orderId = createOrder(auth);

        // RECEIVED -> DELIVERED pula etapas e não é permitido.
        OrderStatusUpdateDto invalidUpdate = new OrderStatusUpdateDto(OrderStatusEnum.DELIVERED);

        mockMvc.perform(patch("/api/order/" + orderId + "/status")
                .with(csrf())
                .cookie(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        // O status original deve ser preservado.
        mockMvc.perform(get("/api/order/" + orderId).cookie(auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void createOrder_WithoutAuthentication_ShouldReturn401() throws Exception {
        OrderCreateDto createDto = new OrderCreateDto(
            "Sem Autenticação",
            "Rua C, 789",
            List.of(new OrderItemDto(null, "X-Burger", new BigDecimal("25.00"), 1, null))
        );

        mockMvc.perform(post("/api/order")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void findAll_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/order"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void request_FromDisallowedOrigin_ShouldBeRejectedByCors() throws Exception {
        // Compensação pelo CSRF desabilitado: o cookie é SameSite=Strict e o CORS
        // só aceita as origens configuradas, então outro site não consegue chamar
        // a API com as credenciais do usuário.
        OrderCreateDto createDto = new OrderCreateDto(
            "Origem Externa",
            "Rua D, 000",
            List.of(new OrderItemDto(null, "X-Burger", new BigDecimal("25.00"), 1, null))
        );

        mockMvc.perform(post("/api/order")
                .header(HttpHeaders.ORIGIN, "http://evil.example")
                .cookie(authCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isForbidden());
    }

    @Test
    void findById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/order/999").cookie(authCookie()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Pedido não encontrado: 999"));
    }

    @Test
    void changeStatus_NotFound_ShouldReturn404() throws Exception {
        OrderStatusUpdateDto statusUpdate = new OrderStatusUpdateDto(OrderStatusEnum.IN_PREPARATION);

        mockMvc.perform(patch("/api/order/999/status")
                .with(csrf())
                .cookie(authCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Pedido não encontrado: 999"));
    }

    private Long createOrder(Cookie auth) throws Exception {
        OrderCreateDto createDto = new OrderCreateDto(
            "Test Client",
            "Test Address",
            List.of(new OrderItemDto(null, "X-Burger", new BigDecimal("25.00"), 1, null))
        );

        String createResponse = mockMvc.perform(post("/api/order")
                .with(csrf())
                .cookie(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(createResponse, OrderDto.class).id();
    }

    /**
     * Token válido para {@code test@email.com}, sem usuário cadastrado no banco.
     * Usado apenas para verificar a autenticação por token.
     */
    private Cookie authCookie() {
        return new Cookie(JwtAuthFilter.ACCESS_COOKIE, jwtService.generateToken("test@email.com"));
    }
}
