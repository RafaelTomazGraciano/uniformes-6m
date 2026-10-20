package com.six_m.uniform.domain.itemLote.dto;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;

import java.util.UUID;

public record ResponseItemLoteDTO(
        UUID id,
        UUID tipoUniformeId,
        String tipoUniformeNome,
        UUID loteId,
        String loteFornecedor,
        Tamanho tamanho,
        Integer quantidade,
        Sexo sexo
) {
}