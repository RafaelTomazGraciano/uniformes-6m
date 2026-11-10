package com.six_m.uniform.shared.enums;

import lombok.Getter;

@Getter
public enum StatusRequisicao {
    PENDENTE("PENDENTE"),
    ATENDIDA("ATENDIDA"),
    CANCELADA("CANCELADA");

    private final String valor;

    StatusRequisicao(String valor) {
        this.valor = valor;
    }
}
