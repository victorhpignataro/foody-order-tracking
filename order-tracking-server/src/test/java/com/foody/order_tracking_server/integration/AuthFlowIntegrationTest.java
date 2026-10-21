package com.foody.order_tracking_server.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.foody.order_tracking_server.dto.auth.LoginRequestDto;
import com.foody.order_tracking_server.dto.auth.RegisterRequestDto;
import com.foody.order_tracking_server.dto.order.OrderCreateDto;
import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;
import com.foody.order_tracking_server.model.User;
import com.foody.order_tracking_server.repository.UserRepository;
import com.foody.order_tracking_server.security.JwtAuthFilter;

import jakarta.servlet.http.Cookie;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testes de integração com a cadeia de filtros de segurança habilitada.
 * Os POST/PATCH enviam o token CSRF via {@code .with(csrf())}; hoje o CSRF está
 * desabilitado de forma deliberada (ver SecurityConfig), mas manter o token nos
 * testes garante que eles continuem válidos caso a política mude.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFlowIntegrationTest {

    private static final String EMAIL = "joao@email.com";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginAndAccessProtectedEndpoint_ShouldWork() throws Exception {
        register("João Silva", EMAIL, PASSWORD)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Success"));

        User saved = userRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(saved.getName()).isEqualTo("João Silva");
        assertThat(saved.getPassword()).isNotEqualTo(PASSWORD);

        Cookie cookie = login(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/order").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createOrder_WithCookieFromRealLogin_ShouldWork() throws Exception {
        register("João Silva", EMAIL, PASSWORD).andExpect(status().isCreated());
        Cookie cookie = login(EMAIL, PASSWORD);

        OrderCreateDto createDto = new OrderCreateDto(
            "João Silva",
            "Rua A, 123",
            List.of(new OrderItemDto(null, "X-Burger", new BigDecimal("25.00"), 2, null)));

        mockMvc.perform(post("/api/order")
                .with(csrf())
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.items[0].name").value("X-Burger"))
            .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void login_ShouldNormalizeEmail() throws Exception {
        register("João Silva", EMAIL, PASSWORD).andExpect(status().isCreated());

        Cookie cookie = login("JOAO@EMAIL.COM", PASSWORD);

        assertThat(cookie.getValue()).isNotBlank();
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldReturn409() throws Exception {
        register("João Silva", EMAIL, PASSWORD).andExpect(status().isCreated());

        register("Outro Nome", EMAIL, PASSWORD)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
            .andExpect(jsonPath("$.message").value(containsString(EMAIL)));
    }

    @Test
    void register_WithInvalidData_ShouldReturn400() throws Exception {
        register("", "not-an-email", "short")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void login_WithWrongPassword_ShouldReturn401() throws Exception {
        register("João Silva", EMAIL, PASSWORD).andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequestDto(EMAIL, "wrong-password"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void protectedEndpoint_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/order"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_ShouldClearCookie() throws Exception {
        register("João Silva", EMAIL, PASSWORD).andExpect(status().isCreated());
        Cookie cookie = login(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/auth/logout").with(csrf()).cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Success"))
            .andExpect(header().string(HttpHeaders.SET_COOKIE,
                containsString(JwtAuthFilter.ACCESS_COOKIE + "=")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    private ResultActions register(String name, String email, String password) throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(name, email, password);
        return mockMvc.perform(post("/api/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }

    private Cookie login(String email, String password) throws Exception {
        LoginRequestDto request = new LoginRequestDto(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Success"))
            .andReturn();

        Cookie cookie = result.getResponse().getCookie(JwtAuthFilter.ACCESS_COOKIE);
        assertThat(cookie).isNotNull();
        return cookie;
    }
}
