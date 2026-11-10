package com.six_m.uniform.domain.requisicao.dto;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.StatusRequisicao;
import com.six_m.uniform.shared.enums.Tamanho;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponseRequisicaoDTO(
        UUID id,
        UUID tipoUniformeId,
        String tipoUniforme,
        Tamanho tamanho,
        Sexo sexo,
        Integer quantidade,
        String observacao,
        StatusRequisicao status,
        UUID usuarioId,
        String usuarioNome,
        LocalDateTime dataCriacao,
        LocalDateTime dataConclusao
) {
}
