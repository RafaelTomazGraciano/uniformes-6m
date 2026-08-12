package com.six_m.uniform.domain.usuario.dto;

import java.util.UUID;

public record ResponseRegistrarUsuario(
        UUID idUsuario,
        String name,
        String email
) {
}
