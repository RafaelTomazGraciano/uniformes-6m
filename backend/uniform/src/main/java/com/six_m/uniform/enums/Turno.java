package com.six_m.uniform.enums;

public enum Turno {
    DIURNO("DIURNO"),
    VESPERTINO("VESPERTINO"),
    NOTURNO("NOTURNO");

    private final String valor;

    Turno(String valor) {
        this.valor = valor;
    }
}