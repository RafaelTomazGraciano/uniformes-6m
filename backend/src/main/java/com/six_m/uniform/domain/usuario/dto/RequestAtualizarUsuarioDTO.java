package com.six_m.uniform.domain.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record RequestAtualizarUsuarioDTO(
        @NotEmpty(message = "Nome é obrigatório")
        String nome,
        @Email(message = "O formato do email é inválido")
        String email
) {
}
