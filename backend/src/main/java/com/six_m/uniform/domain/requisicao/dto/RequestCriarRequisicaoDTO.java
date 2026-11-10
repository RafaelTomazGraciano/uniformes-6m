package com.six_m.uniform.domain.requisicao.dto;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RequestCriarRequisicaoDTO(
        @NotNull(message = "Escolha o tipo de uniforme")
        UUID tipoUniformeId,

        @NotNull(message = "Escolha o tamanho")
        Tamanho tamanho,

        @NotNull(message = "Escolha o sexo")
        Sexo sexo,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
        Integer quantidade,

        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
        String observacao
) {
}
