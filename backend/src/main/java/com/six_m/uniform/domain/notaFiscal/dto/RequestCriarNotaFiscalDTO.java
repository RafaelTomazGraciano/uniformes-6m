package com.six_m.uniform.domain.notaFiscal.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record RequestCriarNotaFiscalDTO(
        @NotEmpty(message = "Chave de acesso é obrigatória")
        String chaveAcesso
) {
}