package com.six_m.uniform.domain.uniforme;

/**
 * Mesma regra da view vw_estoque e de lib/estoque.ts no front: o limite do tipo
 * já conta como baixo (quantidade <= limite), e zero é esgotado.
 */
public enum SituacaoEstoque {
    OK,
    BAIXO,
    ESGOTADO;

    public static SituacaoEstoque calcular(int quantidade, int limite) {
        if (quantidade <= 0) {
            return ESGOTADO;
        }
        return quantidade <= limite ? BAIXO : OK;
    }
}
