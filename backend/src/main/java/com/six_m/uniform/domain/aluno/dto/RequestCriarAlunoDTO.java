package com.six_m.uniform.domain.aluno.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RequestCriarAlunoDTO(
        @NotEmpty(message = "Nome é obrigatório")
        String nome,

        @Size(max = 255, message = "Nome do responsável deve ter no máximo 255 caracteres")
        String nomeResponsavel,

        @Pattern(regexp = "^(\\d{10,11})?$", message = "Telefone deve conter apenas dígitos, com DDD (10 ou 11 dígitos)")
        String telefoneResponsavel,

        @NotNull(message = "Turma é obrigatória")
        UUID turmaId
) {
}