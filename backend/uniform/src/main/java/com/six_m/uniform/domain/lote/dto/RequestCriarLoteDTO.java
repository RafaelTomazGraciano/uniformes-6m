package com.six_m.uniform.domain.lote.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestCriarLoteDTO(
        @NotNull(message = "Nota fiscal é obrigatória")
        UUID notaFiscalId,

        @NotEmpty(message = "Fornecedor é obrigatório")
        String fornecedor,

        LocalDateTime dataEntrega
) {
}