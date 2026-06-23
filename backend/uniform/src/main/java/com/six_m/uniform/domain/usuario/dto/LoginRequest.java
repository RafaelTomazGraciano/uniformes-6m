package com.six_m.uniform.domain.usuario.dto;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
        @NotEmpty(message = "Email é obrigatório")
        String email,
        @NotEmpty(message = "Senha é obrigatório")
        String senha
) {
}
