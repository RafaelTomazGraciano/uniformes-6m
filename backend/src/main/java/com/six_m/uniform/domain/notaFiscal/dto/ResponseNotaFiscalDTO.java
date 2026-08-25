package com.six_m.uniform.domain.notaFiscal.dto;

import java.util.UUID;

public record ResponseNotaFiscalDTO(
        UUID id,
        String chaveAcesso
) {
}