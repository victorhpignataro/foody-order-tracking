package com.foody.order_tracking_server.dto;

public record LoginRequestDto(
        String email,
        String password) {
}