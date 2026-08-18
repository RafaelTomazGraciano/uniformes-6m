package com.six_m.uniform.domain.usuario.dto;

import java.util.UUID;

public record ResponseUsuarioDTO(
        UUID idUsuario,
        String nome,
        String email
) {
}
