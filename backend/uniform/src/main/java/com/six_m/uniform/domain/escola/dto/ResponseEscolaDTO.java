package com.six_m.uniform.domain.escola.dto;

import com.six_m.uniform.shared.enums.TipoEscola;

import java.util.UUID;

public record ResponseEscolaDTO(
        UUID id,
        String nome,
        TipoEscola tipo,
        String endereco
) {
}
