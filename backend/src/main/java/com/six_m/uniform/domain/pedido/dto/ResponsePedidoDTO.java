package com.six_m.uniform.domain.pedido.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsePedidoDTO(
        UUID id,
        UUID alunoId,
        String alunoNome,
        UUID usuarioId,
        String usuarioNome,
        LocalDateTime dataEfetivada
) {
}