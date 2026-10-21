package com.foody.order_tracking_server.dto.orderitem;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderItemDto(
    Long id,

    @NotBlank(message = "O nome do item é obrigatório")
    @Size(max = 256, message = "O nome do item deve conter no máximo 256 caracteres")
    String name,

    @NotNull(message = "O preço unitário é obrigatório")
    @DecimalMin(value = "0.00", message = "O preço unitário não pode ser negativo")
    BigDecimal unitPrice,

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    Integer quantity,

    Long orderId) {

}
