package com.foody.order_tracking_server.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.foody.order_tracking_server.dto.auth.LoginRequestDto;
import com.foody.order_tracking_server.dto.auth.RegisterRequestDto;
import com.foody.order_tracking_server.service.AuthService;
import com.foody.order_tracking_server.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto("João Silva", "joao@email.com", "password123");
        loginRequest = new LoginRequestDto("joao@email.com", "password123");
    }

    @Test
    void register_WithValidData_ShouldReturn201() throws Exception {
        doNothing().when(authService).register(any(RegisterRequestDto.class));

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"));

        verify(authService).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_WithInvalidData_ShouldReturn400() throws Exception {
        RegisterRequestDto invalidDto = new RegisterRequestDto("", "invalid-email", "short");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldReturn409() throws Exception {
        doThrow(new com.foody.order_tracking_server.exception.EmailAlreadyExistsException("joao@email.com"))
                .when(authService).register(any(RegisterRequestDto.class));

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("The requested email already exists: joao@email.com"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturn200AndSetCookie() throws Exception {
        AuthService.LoginResult loginResult = new AuthService.LoginResult("jwt-token", "João Silva");
        when(authService.authenticate(any(LoginRequestDto.class))).thenReturn(loginResult);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("access_token=jwt-token")));

        verify(authService).authenticate(any(LoginRequestDto.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        when(authService.authenticate(any(LoginRequestDto.class)))
                .thenThrow(new com.foody.order_tracking_server.exception.InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithInvalidData_ShouldReturn400() throws Exception {
        LoginRequestDto invalidDto = new LoginRequestDto("", "");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void logout_ShouldReturn200AndClearCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(
                        header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("access_token=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}