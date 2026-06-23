package com.six_m.uniform.domain.usuario.dto;

import java.util.UUID;

public record ResponseUsuarioDTO(
        UUID idUsuario,
        String name,
        String email
) {
}
