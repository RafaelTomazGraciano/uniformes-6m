package com.six_m.uniform.domain.pedidoUniforme.dto;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;

import java.util.UUID;

public record ResponsePedidoUniformeDTO(
        UUID id,
        UUID pedidoId,
        UUID uniformeId,
        String uniformeTipoUniformeNome,
        Tamanho uniformeTamanho,
        Sexo uniformeSexo,
        Integer quantidade
) {
}