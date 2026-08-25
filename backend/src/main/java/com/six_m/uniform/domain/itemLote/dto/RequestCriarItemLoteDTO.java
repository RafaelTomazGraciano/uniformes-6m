package com.six_m.uniform.domain.itemLote.dto;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RequestCriarItemLoteDTO(
        @NotNull(message = "Tipo de uniforme é obrigatório")
        UUID tipoUniformeId,

        @NotNull(message = "Lote é obrigatório")
        UUID loteId,

        @NotNull(message = "Tamanho é obrigatório")
        Tamanho tamanho,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 0, message = "Quantidade não pode ser negativa")
        Integer quantidade,

        @NotNull(message = "Sexo é obrigatório")
        Sexo sexo
) {
}