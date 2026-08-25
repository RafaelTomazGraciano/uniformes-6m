package com.six_m.uniform.domain.tipoUniforme.dto;

import java.util.UUID;

public record ResponseTipoUniformeDTO(
        UUID id,
        String tipo
) {
}