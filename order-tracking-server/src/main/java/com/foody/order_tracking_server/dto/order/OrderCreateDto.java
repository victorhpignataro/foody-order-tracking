package com.foody.order_tracking_server.dto.order;

import java.util.List;

import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderCreateDto(
        @NotBlank(message = "O cliente é obrigatório") @Size(max = 256, message = "O cliente deve conter no máximo 256 caracteres") String client,

        @NotBlank(message = "O endereço é obrigatório") @Size(max = 256, message = "O endereço deve conter no máximo 256 caracteres") String address,

        @NotEmpty(message = "O pedido deve conter pelo menos um item") List<@NotNull(message = "O item do pedido não pode ser nulo") @Valid OrderItemDto> items) {

    public OrderCreateDto {
        items = items == null ? List.of() : items;
    }
}
