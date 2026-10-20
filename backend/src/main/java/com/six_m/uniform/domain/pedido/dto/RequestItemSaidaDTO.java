package com.six_m.uniform.domain.pedido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RequestItemSaidaDTO(
        @NotNull(message = "Uniforme é obrigatório")
        UUID uniformeId,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
        Integer quantidade
) {
}