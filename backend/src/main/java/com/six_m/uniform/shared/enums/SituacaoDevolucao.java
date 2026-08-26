package com.six_m.uniform.shared.enums;

public enum SituacaoDevolucao {
    BOM_ESTADO("BOM_ESTADO"),
    DANIFICADO("DANIFICADO"),
    PERDIDO("PERDIDO");

    private final String valor;

    SituacaoDevolucao(String valor) {
        this.valor = valor;
    }
}
