package com.six_m.uniform.domain.pedido.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestAtualizarPedidoDTO(
        @NotNull(message = "Aluno é obrigatório")
        UUID alunoId,

        LocalDateTime dataEfetivada
) {
}