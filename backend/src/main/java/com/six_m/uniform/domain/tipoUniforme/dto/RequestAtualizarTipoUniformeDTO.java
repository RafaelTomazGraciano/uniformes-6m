package com.six_m.uniform.domain.tipoUniforme.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestAtualizarTipoUniformeDTO(
        @NotBlank(message = "Tipo é obrigatório")
        String tipo
) {
}