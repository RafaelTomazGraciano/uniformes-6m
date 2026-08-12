package com.six_m.uniform.enums;

import lombok.Getter;

@Getter
public enum TipoEscola {
    PUBLICA("PUBLICA"),
    TECNICA("TECNICA"),
    PARCEIRA("PARCEIRA"),
    CIVICO_MILITAR("CIVICO_MILITAR"),
    MILITAR("MILITAR");

    private final String valor;

    TipoEscola(String valor) {
        this.valor = valor;
    }
}
