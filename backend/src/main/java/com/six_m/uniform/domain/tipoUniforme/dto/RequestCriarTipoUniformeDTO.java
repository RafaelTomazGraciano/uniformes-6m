package com.six_m.uniform.domain.tipoUniforme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RequestCriarTipoUniformeDTO(
        @NotBlank(message = "Tipo é obrigatório")
        String tipo,

        @NotNull(message = "Estoque baixo é obrigatório")
        @PositiveOrZero(message = "Estoque baixo não pode ser negativo")
        Integer estoqueBaixo
) {
}