package com.six_m.uniform.shared.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.util.List;

@Component
public class PdfTableBuilder {

    public byte[] gerarPdf(String titulo, String descricaoPeriodo, List<String> cabecalhos,
                           List<List<String>> linhas, List<String> totais) {
        try {
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            adicionarTitulo(document, titulo);
            if (descricaoPeriodo != null) {
                adicionarDescricaoPeriodo(document, descricaoPeriodo);
            }
            adicionarTabela(document, cabecalhos, linhas);
            if (totais != null && !totais.isEmpty()) {
                adicionarTotais(document, totais);
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException exception) {
            throw new RuntimeException("Erro ao gerar relatório em PDF", exception);
        }
    }

    private void adicionarTitulo(Document document, String titulo) throws DocumentException {
        Font fonteTitulo = new Font(Font.HELVETICA, 16, Font.BOLD);
        Paragraph paragrafo = new Paragraph(titulo, fonteTitulo);
        paragrafo.setSpacingAfter(10);
        document.add(paragrafo);
    }

    private void adicionarDescricaoPeriodo(Document document, String descricao) throws DocumentException {
        Font fontePeriodo = new Font(Font.HELVETICA, 11, Font.ITALIC);
        Paragraph paragrafo = new Paragraph(descricao, fontePeriodo);
        paragrafo.setSpacingAfter(15);
        document.add(paragrafo);
    }

    private void adicionarTabela(Document document, List<String> cabecalhos, List<List<String>> linhas) throws DocumentException {
        PdfPTable tabela = new PdfPTable(cabecalhos.size());
        tabela.setWidthPercentage(100);

        Font fonteCabecalho = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        for (String cabecalho : cabecalhos) {
            PdfPCell celula = new PdfPCell(new Phrase(cabecalho, fonteCabecalho));
            celula.setBackgroundColor(new Color(51, 51, 51));
            celula.setPadding(6);
            tabela.addCell(celula);
        }

        Font fonteLinha = new Font(Font.HELVETICA, 10);
        for (List<String> linha : linhas) {
            for (String valor : linha) {
                PdfPCell celula = new PdfPCell(new Phrase(valor, fonteLinha));
                celula.setPadding(5);
                tabela.addCell(celula);
            }
        }

        document.add(tabela);
    }

    private void adicionarTotais(Document document, List<String> totais) throws DocumentException {
        document.add(new Paragraph(" "));
        Font fonteTotal = new Font(Font.HELVETICA, 11, Font.BOLD);
        for (String linha : totais) {
            document.add(new Paragraph(linha, fonteTotal));
        }
    }
}