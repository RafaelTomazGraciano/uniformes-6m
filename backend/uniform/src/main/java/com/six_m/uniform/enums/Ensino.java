package com.six_m.uniform.enums;

public enum Ensino {
    FUNDAMENTAL("FUNDAMENTAL"),
    MEDIO("MEDIO"),
    TECNICO("TECNICO");

    private final String valor;

    Ensino(String valor) {
        this.valor = valor;
    }
}