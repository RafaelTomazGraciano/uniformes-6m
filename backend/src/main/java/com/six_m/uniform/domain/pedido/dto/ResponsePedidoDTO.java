package com.six_m.uniform.domain.pedido.dto;

import com.six_m.uniform.domain.pedidoUniforme.dto.ResponsePedidoUniformeDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ResponsePedidoDTO(
        UUID id,
        UUID alunoId,
        String alunoNome,
        UUID usuarioId,
        String usuarioNome,
        LocalDateTime dataEfetivada,
        List<ResponsePedidoUniformeDTO> itens
) {
}