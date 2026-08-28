package com.six_m.uniform.domain.lote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

public record RequestCriarLoteDTO(
        @NotEmpty(message = "Chave de acesso é obrigatória")
        String chaveAcesso,

        @NotEmpty(message = "Fornecedor é obrigatório")
        String fornecedor,

        LocalDateTime dataEntrega,

        @NotEmpty(message = "O lote precisa ter ao menos um item")
        @Valid
        List<RequestItemEntradaDTO> itens
) {
}