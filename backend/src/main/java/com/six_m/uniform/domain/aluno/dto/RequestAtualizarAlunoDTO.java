package com.six_m.uniform.domain.aluno.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record RequestAtualizarAlunoDTO(
        @NotEmpty(message = "Nome é obrigatório")
        String nome,

        String nomeResponsavel,

        @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve conter apenas dígitos, com DDD (10 ou 11 dígitos)")
        String telefoneResponsavel,

        @NotNull(message = "Turma é obrigatória")
        UUID turmaId
) {
}