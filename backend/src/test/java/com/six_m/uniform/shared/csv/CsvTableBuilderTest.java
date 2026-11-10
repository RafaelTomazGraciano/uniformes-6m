package com.six_m.uniform.shared.csv;

import com.six_m.uniform.shared.pdf.PdfTableBuilder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvTableBuilderTest {

    private final CsvTableBuilder csvTableBuilder = new CsvTableBuilder();

    @Test
    void deveGerarCsvComBomSeparadorPontoEVirgulaECrlf() {
        byte[] csv = csvTableBuilder.gerarCsv(
                "Relatório de Saída de Uniformes",
                "Período: 01/03/2026 a 31/03/2026",
                List.of("Data", "Aluno", "Quantidade"),
                List.of(List.of("15/03/2026", "João da Silva", "5")),
                List.of("Total de saídas: 5 unidades"));

        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, java.util.Arrays.copyOf(csv, 3));

        String texto = new String(csv, StandardCharsets.UTF_8).substring(1);
        assertEquals("""
                Relatório de Saída de Uniformes\r
                Período: 01/03/2026 a 31/03/2026\r
                \r
                Data;Aluno;Quantidade\r
                15/03/2026;João da Silva;5\r
                \r
                Total de saídas: 5 unidades\r
                """, texto);
    }

    @Test
    void deveGerarCsvComCabecalhoEIndicacaoDeNenhumRegistroQuandoNaoHaLinhas() {
        String texto = new String(csvTableBuilder.gerarCsv(
                "Relatório de Estoque de Uniformes", null,
                List.of("Tipo", "Tamanho"), List.of(), null), StandardCharsets.UTF_8);

        assertTrue(texto.contains("Tipo;Tamanho\r\nNenhum registro encontrado\r\n"));
    }

    @Test
    void deveEscaparSeparadorAspasEFormulas() {
        assertEquals("\"Silva; João\"", CsvTableBuilder.escapar("Silva; João"));
        assertEquals("\"Camiseta \"\"polo\"\"\"", CsvTableBuilder.escapar("Camiseta \"polo\""));
        assertEquals("'=HYPERLINK(1)", CsvTableBuilder.escapar("=HYPERLINK(1)"));
        assertEquals("", CsvTableBuilder.escapar(null));
    }

    @Test
    void pdfVazioTambemDeveSerGeradoSemErro() {
        byte[] pdf = new PdfTableBuilder().gerarPdf("Relatório de Estoque de Uniformes", null,
                List.of("Tipo", "Tamanho"), List.of(), null);

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }
}
