package com.foody.order_tracking_server.exception;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.foody.order_tracking_server.controller.OrderController;
import com.foody.order_tracking_server.model.OrderStatusEnum;
import com.foody.order_tracking_server.service.JwtService;
import com.foody.order_tracking_server.service.OrderService;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void handleOrderNotFoundException_ShouldReturn404() throws Exception {
        when(orderService.findById(999L))
                .thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/api/order/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Pedido não encontrado: 999"));
    }

    @Test
    void handleValidationException_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/order")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "client": "",
                            "address": "",
                            "items": []
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void handleInvalidStatusTransition_ShouldReturn409() throws Exception {
        when(orderService.changeStatus(eq(1L), eq(OrderStatusEnum.DELIVERED)))
                .thenThrow(new InvalidStatusTransitionException(
                        OrderStatusEnum.RECEIVED, OrderStatusEnum.DELIVERED));

        mockMvc.perform(patch("/api/order/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "status": "DELIVERED" }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }

    @Test
    void handleAccessDenied_ShouldReturn403() throws Exception {
        when(orderService.findById(1L))
                .thenThrow(new AccessDeniedException("negado"));

        mockMvc.perform(get("/api/order/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void handleUnsupportedMethod_ShouldPreserve405() throws Exception {
        mockMvc.perform(delete("/api/order"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void handleUnexpectedException_ShouldReturn500WithoutInternalDetails() throws Exception {
        when(orderService.findById(1L))
                .thenThrow(new IllegalStateException("detalhe interno que não deve vazar"));

        mockMvc.perform(get("/api/order/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Erro interno inesperado."))
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void malformedJson_ShouldReturn400WithStandardBody() throws Exception {
        mockMvc.perform(post("/api/order")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"client\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }
}
