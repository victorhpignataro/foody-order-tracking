package com.foody.order_tracking_server.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.foody.order_tracking_server.service.JwtService;
import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    private JwtAuthFilter jwtAuthFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws IOException, jakarta.servlet.ServletException {
        String token = "valid-jwt-token";
        request.setCookies(new jakarta.servlet.http.Cookie(JwtAuthFilter.ACCESS_COOKIE, token));

        when(jwtService.extractLogin(token)).thenReturn("user@email.com");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("user@email.com");
        verify(jwtService).extractLogin(token);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws IOException, jakarta.servlet.ServletException {
        String token = "invalid-jwt-token";
        request.setCookies(new jakarta.servlet.http.Cookie(JwtAuthFilter.ACCESS_COOKIE, token));

        when(jwtService.extractLogin(token)).thenThrow(new JwtException("Invalid token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService).extractLogin(token);
    }

    @Test
    void doFilterInternal_WithoutToken_ShouldNotSetAuthentication() throws IOException, jakarta.servlet.ServletException {
        // No cookie set
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_WithNullCookies_ShouldNotSetAuthentication() throws IOException, jakarta.servlet.ServletException {
        request.setCookies((jakarta.servlet.http.Cookie[]) null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_WithOtherCookiesButNotAccessToken_ShouldNotSetAuthentication() throws IOException, jakarta.servlet.ServletException {
        request.setCookies(new jakarta.servlet.http.Cookie("other_cookie", "value"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_WhenAuthenticationAlreadyExists_ShouldNotOverride() throws IOException, jakarta.servlet.ServletException {
        String token = "valid-jwt-token";
        request.setCookies(new jakarta.servlet.http.Cookie(JwtAuthFilter.ACCESS_COOKIE, token));

        // Pre-set authentication
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("existing@email.com", null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Should keep existing authentication
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("existing@email.com");
        verifyNoInteractions(jwtService);
    }

    @Test
    void extractToken_WithValidCookie_ShouldReturnToken() {
        String token = "test-token";
        request.setCookies(new jakarta.servlet.http.Cookie(JwtAuthFilter.ACCESS_COOKIE, token));

        String extracted = invokeExtractToken(request);

        assertThat(extracted).isEqualTo(token);
    }

    @Test
    void extractToken_WithMultipleCookies_ShouldReturnAccessToken() {
        request.setCookies(
            new jakarta.servlet.http.Cookie("other", "value1"),
            new jakarta.servlet.http.Cookie(JwtAuthFilter.ACCESS_COOKIE, "access-token"),
            new jakarta.servlet.http.Cookie("another", "value2")
        );

        String extracted = invokeExtractToken(request);

        assertThat(extracted).isEqualTo("access-token");
    }

    // Helper method to test private method
    private String invokeExtractToken(MockHttpServletRequest request) {
        try {
            java.lang.reflect.Method method = JwtAuthFilter.class.getDeclaredMethod("extractToken", jakarta.servlet.http.HttpServletRequest.class);
            method.setAccessible(true);
            return (String) method.invoke(jwtAuthFilter, request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}