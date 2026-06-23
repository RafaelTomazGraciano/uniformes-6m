package com.six_m.uniform.domain.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RequestRegistrarUsuario(
        @NotEmpty(message = "Nome é obrigatório")
        String nome,
        @Email(message = "O formato do email é inválido")
        String email,
        @NotEmpty(message = "Senha é obrigatório")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha
) {
}
