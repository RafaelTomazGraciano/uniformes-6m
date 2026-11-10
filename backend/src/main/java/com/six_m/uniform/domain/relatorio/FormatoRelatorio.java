package com.six_m.uniform.domain.relatorio;

import com.six_m.uniform.exception.BadRequestException;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public enum FormatoRelatorio {
    PDF("pdf", MediaType.APPLICATION_PDF),
    CSV("csv", new MediaType("text", "csv", StandardCharsets.UTF_8));

    private final String extensao;
    private final MediaType mediaType;

    FormatoRelatorio(String extensao, MediaType mediaType) {
        this.extensao = extensao;
        this.mediaType = mediaType;
    }

    public String extensao() {
        return extensao;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    /** Aceita "pdf"/"csv" em qualquer caixa; ausente vale PDF, que era o único formato antes. */
    public static FormatoRelatorio de(String valor) {
        if (valor == null || valor.isBlank()) {
            return PDF;
        }
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato de relatório inválido: use pdf ou csv");
        }
    }
}
