package com.six_m.uniform.domain.aluno.dto;

import java.util.UUID;

public record ResponseAlunoDTO(
        UUID id,
        String nome,
        UUID turmaId,
        String turmaNome
) {
}