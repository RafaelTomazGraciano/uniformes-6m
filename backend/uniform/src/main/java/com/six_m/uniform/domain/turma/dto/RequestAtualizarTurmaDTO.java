package com.six_m.uniform.domain.turma.dto;

import com.six_m.uniform.shared.enums.Ensino;
import com.six_m.uniform.shared.enums.Turno;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RequestAtualizarTurmaDTO(
        @NotEmpty(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Turno é obrigatório")
        Turno turno,

        @NotNull(message = "Ensino é obrigatório")
        Ensino ensino
) {
}