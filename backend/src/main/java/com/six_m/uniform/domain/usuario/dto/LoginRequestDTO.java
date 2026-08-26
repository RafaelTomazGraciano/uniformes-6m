package com.six_m.uniform.domain.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginRequestDTO(
        @Email(message = "O formato do email é inválido")
        String email,
        @NotEmpty(message = "Senha é obrigatório")
        String senha
) {
}
