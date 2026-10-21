package com.foody.order_tracking_server.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foody.order_tracking_server.dto.MessageResponseDto;
import com.foody.order_tracking_server.dto.auth.LoginRequestDto;
import com.foody.order_tracking_server.dto.auth.RegisterRequestDto;
import com.foody.order_tracking_server.security.JwtAuthFilter;
import com.foody.order_tracking_server.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final long expirationMs;
  private final boolean cookieSecure;

  public AuthController(
      AuthService authService,
      @Value("${jwt.expiration-ms}") long expirationMs,
      @Value("${cookie.secure:false}") boolean cookieSecure) {
    this.authService = authService;
    this.expirationMs = expirationMs;
    this.cookieSecure = cookieSecure;
  }

  @PostMapping("/register")
  public ResponseEntity<MessageResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
    authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new MessageResponseDto("Success"));
  }

  @PostMapping("/login")
  public ResponseEntity<MessageResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
    AuthService.LoginResult result = authService.authenticate(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, buildCookie(result.token(), expirationMs).toString())
        .body(new MessageResponseDto("Success"));
  }

  @PostMapping("/logout")
  public ResponseEntity<MessageResponseDto> logout() {
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, buildCookie("", 0).toString())
        .body(new MessageResponseDto("Success"));
  }

  private ResponseCookie buildCookie(String value, long maxAgeMs) {
    return ResponseCookie.from(JwtAuthFilter.ACCESS_COOKIE, value)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofMillis(maxAgeMs))
        .build();
  }
}