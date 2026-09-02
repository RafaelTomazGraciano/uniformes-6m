package com.six_m.uniform.domain.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RequestTrocarSenhaDTO(
        @Email(message = "O formato do email é inválido")
        @NotEmpty(message = "Email é obrigatório")
        String email,

        @NotEmpty(message = "Nova senha é obrigatória")
        @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
        String novaSenha
) {
}