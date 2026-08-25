package com.six_m.uniform.domain.uniforme.dto;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;

import java.util.UUID;

public record ResponseUniformeDTO(
        UUID id,
        UUID tipoUniformeId,
        String tipoUniformeNome,
        Tamanho tamanho,
        Integer quantidade,
        Sexo sexo,
        Boolean devolvido
) {
}