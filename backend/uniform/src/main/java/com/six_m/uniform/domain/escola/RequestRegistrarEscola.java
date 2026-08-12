package com.six_m.uniform.domain.escola;

import jakarta.validation.constraints.NotEmpty;

public record RequestRegistrarEscola(
        @NotEmpty(message = "Nome é obrigatório")
        String nome,
        @NotEmpty(message = "Tipo do uniforme é obrigatório")
        String tipo
) {
}
