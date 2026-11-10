package com.six_m.uniform.domain.uniforme;

import java.util.UUID;

/** Projeção de uma linha da view vw_estoque (V4__view_estoque.sql). */
public interface EstoqueView {
    UUID getUniformeId();

    UUID getTipoUniformeId();

    String getTipo();

    String getTamanho();

    String getSexo();

    Integer getQuantidade();

    Integer getEstoqueBaixo();

    String getSituacao();
}
