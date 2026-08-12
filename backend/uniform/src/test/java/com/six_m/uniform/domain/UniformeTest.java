package com.six_m.uniform.domain;

import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.enums.Sexo;
import com.six_m.uniform.enums.Tamanho;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UniformeTest {

    @Test
    void deveMarcarComoDevolvidoAoChamarDevolver() {
        Uniforme uniforme = new Uniforme();
        uniforme.setDevolvido(false);

        uniforme.devolver();

        assertTrue(uniforme.getDevolvido());
    }

    @Test
    void devolverDeveSerIdempotente() {
        Uniforme uniforme = new Uniforme();
        uniforme.setDevolvido(true);

        uniforme.devolver();

        assertTrue(uniforme.getDevolvido());
    }

    @Test
    void uniformeDeveComecarComoNaoDevolvidoPorPadrao() {
        Uniforme uniforme = Uniforme.builder()
                .tamanho(Tamanho.M)
                .sexo(Sexo.MASCULINO)
                .quantidade(10)
                .build();

        // Se você adicionar @Builder.Default = false no campo, este teste confirma
        assertFalse(uniforme.getDevolvido() != null && uniforme.getDevolvido());
    }
}
