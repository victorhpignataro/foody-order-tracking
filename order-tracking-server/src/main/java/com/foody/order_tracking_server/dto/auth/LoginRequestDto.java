package com.foody.order_tracking_server.dto.auth;

public record LoginRequestDto(
        String email,
        String password) {
}