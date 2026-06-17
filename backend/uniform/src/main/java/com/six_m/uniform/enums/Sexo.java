package com.six_m.uniform.enums;

import lombok.Getter;

@Getter
public enum Sexo {
    MASCULINO("MASCULINO"),
    FEMININO("FEMININO");

    private final String valor;

    Sexo(String valor) {
        this.valor = valor;
    }
}
