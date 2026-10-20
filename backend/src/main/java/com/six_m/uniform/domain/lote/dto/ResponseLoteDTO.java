package com.six_m.uniform.domain.lote.dto;

import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ResponseLoteDTO(
        UUID id,
        UUID notaFiscalId,
        String notaFiscalChaveAcesso,
        String fornecedor,
        LocalDateTime dataEntrega,
        List<ResponseItemLoteDTO> itens
) {
}