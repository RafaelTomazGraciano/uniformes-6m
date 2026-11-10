package com.six_m.uniform.shared.csv;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gera o CSV no formato que o Excel em pt-BR abre direto com dois cliques:
 * UTF-8 com BOM (sem ele os acentos viram lixo), ";" como separador (a vírgula
 * é o separador decimal no Brasil) e quebra de linha CRLF.
 * Datas e números chegam já formatados em pt-BR pelo serviço de relatórios.
 */
@Component
public class CsvTableBuilder {

    public static final String NENHUM_REGISTRO = "Nenhum registro encontrado";

    private static final String BOM = "﻿";
    private static final String SEPARADOR = ";";
    private static final String QUEBRA = "\r\n";

    public byte[] gerarCsv(String titulo, String descricaoPeriodo, List<String> cabecalhos,
                           List<List<String>> linhas, List<String> totais) {
        StringBuilder csv = new StringBuilder(BOM);

        adicionarLinha(csv, List.of(titulo));
        if (descricaoPeriodo != null) {
            adicionarLinha(csv, List.of(descricaoPeriodo));
        }
        csv.append(QUEBRA);

        adicionarLinha(csv, cabecalhos);
        if (linhas.isEmpty()) {
            adicionarLinha(csv, List.of(NENHUM_REGISTRO));
        }
        for (List<String> linha : linhas) {
            adicionarLinha(csv, linha);
        }

        if (totais != null && !totais.isEmpty()) {
            csv.append(QUEBRA);
            for (String total : totais) {
                adicionarLinha(csv, List.of(total));
            }
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void adicionarLinha(StringBuilder csv, List<String> valores) {
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                csv.append(SEPARADOR);
            }
            csv.append(escapar(valores.get(i)));
        }
        csv.append(QUEBRA);
    }

    static String escapar(String valor) {
        if (valor == null) {
            return "";
        }

        /* Um nome começando com "=" seria executado como fórmula pelo Excel
           (injeção de CSV); o apóstrofo faz a célula ser lida como texto. */
        String seguro = !valor.isEmpty() && "=+-@\t\r".indexOf(valor.charAt(0)) >= 0 ? "'" + valor : valor;

        if (seguro.contains(SEPARADOR) || seguro.contains("\"") || seguro.contains("\n") || seguro.contains("\r")) {
            return "\"" + seguro.replace("\"", "\"\"") + "\"";
        }
        return seguro;
    }
}
