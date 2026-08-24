package com.six_m.uniform.domain.lote.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponseLoteDTO(
        UUID id,
        UUID notaFiscalId,
        String notaFiscalChaveAcesso,
        String fornecedor,
        LocalDateTime dataEntrega
) {
}