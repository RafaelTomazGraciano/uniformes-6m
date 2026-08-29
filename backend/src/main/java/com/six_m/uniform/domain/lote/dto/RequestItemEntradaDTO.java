package com.six_m.uniform.domain.lote.dto;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RequestItemEntradaDTO(
        @NotNull(message = "Tipo de uniforme é obrigatório")
        UUID tipoUniformeId,

        @NotNull(message = "Tamanho é obrigatório")
        Tamanho tamanho,

        @NotNull(message = "Sexo é obrigatório")
        Sexo sexo,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
        Integer quantidade
) {
}