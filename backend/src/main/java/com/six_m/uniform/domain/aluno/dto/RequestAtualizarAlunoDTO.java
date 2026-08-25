package com.six_m.uniform.domain.aluno.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RequestAtualizarAlunoDTO(
        @NotEmpty(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Turma é obrigatória")
        UUID turmaId
) {
}