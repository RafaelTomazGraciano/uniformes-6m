package com.six_m.uniform.domain.uniforme.dto;

import com.six_m.uniform.domain.uniforme.SituacaoEstoque;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;

import java.util.UUID;

public record ResponseEstoquePainelDTO(
        UUID uniformeId,
        UUID tipoUniformeId,
        String tipoUniformeNome,
        Tamanho tamanho,
        Sexo sexo,
        Integer quantidade,
        Integer estoqueBaixo,
        SituacaoEstoque situacao
) {
}
