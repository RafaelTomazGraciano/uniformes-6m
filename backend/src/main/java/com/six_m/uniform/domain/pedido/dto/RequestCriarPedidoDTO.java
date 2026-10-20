package com.six_m.uniform.domain.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RequestCriarPedidoDTO(
        @NotNull(message = "Aluno é obrigatório")
        UUID alunoId,

        LocalDateTime dataEfetivada,

        @NotEmpty(message = "O pedido precisa ter ao menos um item")
        @Valid
        List<RequestItemSaidaDTO> itens
) {
}