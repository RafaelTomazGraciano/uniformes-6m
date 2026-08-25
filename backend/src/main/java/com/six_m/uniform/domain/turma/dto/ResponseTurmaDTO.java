package com.six_m.uniform.domain.turma.dto;

import com.six_m.uniform.shared.enums.Ensino;
import com.six_m.uniform.shared.enums.Turno;

import java.util.UUID;

public record ResponseTurmaDTO(
        UUID id,
        String nome,
        Turno turno,
        Ensino ensino
) {
}
