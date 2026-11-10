package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;

import java.util.Comparator;
import java.util.UUID;

/** Identifica uma linha de estoque pela combinação tipo + tamanho + sexo (a chave única da tabela uniforme). */
public record CombinacaoUniforme(UUID tipoUniformeId, Tamanho tamanho, Sexo sexo) implements Comparable<CombinacaoUniforme> {

    private static final Comparator<CombinacaoUniforme> ORDEM = Comparator
            .comparing(CombinacaoUniforme::tipoUniformeId)
            .thenComparing(CombinacaoUniforme::tamanho)
            .thenComparing(CombinacaoUniforme::sexo);

    @Override
    public int compareTo(CombinacaoUniforme outra) {
        return ORDEM.compare(this, outra);
    }
}
