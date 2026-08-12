package com.six_m.uniform.enums;

import lombok.Getter;

@Getter
public enum Tamanho {
    PP("PP"),
    P("P"),
    M("M"),
    G("G"),
    GG("GG");

    private final String valor;

    Tamanho(String valor) {
        this.valor = valor;
    }
}