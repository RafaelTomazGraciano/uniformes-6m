package com.six_m.uniform.domain.tipoUniforme.dto;

import jakarta.validation.constraints.NotEmpty;

public record RequestAtualizarTipoUniformeDTO(
        @NotEmpty(message = "Tipo é obrigatório")
        String tipo
) {
}