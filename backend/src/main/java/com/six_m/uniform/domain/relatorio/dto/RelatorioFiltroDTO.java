package com.six_m.uniform.domain.relatorio.dto;

import com.six_m.uniform.shared.enums.TipoFiltroRelatorio;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RelatorioFiltroDTO(
        @NotNull(message = "Tipo de filtro é obrigatório")
        TipoFiltroRelatorio tipo,

        @NotNull(message = "Ano de início é obrigatório")
        @Min(value = 2000, message = "Ano inválido")
        Integer anoInicio,

        @Min(value = 1, message = "Mês deve ser entre 1 e 12")
        @Max(value = 12, message = "Mês deve ser entre 1 e 12")
        Integer mesInicio,

        @Min(value = 2000, message = "Ano inválido")
        Integer anoFim,

        @Min(value = 1, message = "Mês deve ser entre 1 e 12")
        @Max(value = 12, message = "Mês deve ser entre 1 e 12")
        Integer mesFim
) {
}