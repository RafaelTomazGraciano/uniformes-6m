package com.six_m.uniform.domain.usuario.dto;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequestDTO(
        @NotEmpty(message = "Email é obrigatório")
        String email,
        @NotEmpty(message = "Senha é obrigatório")
        String senha
) {
}
