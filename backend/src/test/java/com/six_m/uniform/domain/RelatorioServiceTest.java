package com.six_m.uniform.domain;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.pedido.Pedido;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeService;
import com.six_m.uniform.domain.relatorio.FormatoRelatorio;
import com.six_m.uniform.domain.relatorio.Periodo;
import com.six_m.uniform.domain.relatorio.RelatorioPeriodoResolver;
import com.six_m.uniform.domain.relatorio.RelatorioService;
import com.six_m.uniform.domain.relatorio.dto.RelatorioFiltroDTO;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.turma.Turma;
import com.six_m.uniform.domain.uniforme.SituacaoEstoque;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.domain.uniforme.dto.ResponseEstoquePainelDTO;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import com.six_m.uniform.shared.enums.TipoFiltroRelatorio;
import com.six_m.uniform.shared.csv.CsvTableBuilder;
import com.six_m.uniform.shared.pdf.PdfTableBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RelatorioServiceTest {

    @Mock
    private UniformeService uniformeService;

    @Mock
    private ItemLoteService itemLoteService;

    @Mock
    private PedidoUniformeService pedidoUniformeService;

    @Mock
    private RelatorioPeriodoResolver periodoResolver;

    @Mock
    private PdfTableBuilder pdfTableBuilder;

    @Mock
    private CsvTableBuilder csvTableBuilder;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void deveGerarRelatorioEstoqueComSucessoAPartirDaView() {
        when(uniformeService.buscarPainelEstoque()).thenReturn(List.of(linhaEstoque(10, 5, SituacaoEstoque.OK)));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1, 2, 3});

        byte[] resultado = relatorioService.gerarRelatorioEstoque(FormatoRelatorio.PDF);

        assertNotNull(resultado);
        ArgumentCaptor<List<List<String>>> linhasCaptor = ArgumentCaptor.forClass(List.class);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Estoque de Uniformes"), isNull(),
                eq(List.of("Tipo", "Tamanho", "Sexo", "Quantidade", "Limite", "Situação")), linhasCaptor.capture(),
                eq(List.of("Total em estoque: 10 unidades")));

        assertEquals(List.of("Camiseta", "M", "Masculino", "10", "5", "OK"), linhasCaptor.getValue().getFirst());
        verifyNoInteractions(csvTableBuilder);
    }

    @Test
    void deveUsarASituacaoDaViewNoRelatorioDeEstoque() {
        when(uniformeService.buscarPainelEstoque()).thenReturn(List.of(
                linhaEstoque(5, 5, SituacaoEstoque.BAIXO),
                linhaEstoque(0, 5, SituacaoEstoque.ESGOTADO)));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        relatorioService.gerarRelatorioEstoque(FormatoRelatorio.PDF);

        ArgumentCaptor<List<List<String>>> linhasCaptor = ArgumentCaptor.forClass(List.class);
        verify(pdfTableBuilder).gerarPdf(any(), any(), any(), linhasCaptor.capture(), any());

        assertEquals("Baixo", linhasCaptor.getValue().get(0).get(5));
        assertEquals("Esgotado", linhasCaptor.getValue().get(1).get(5));
    }

    @Test
    void deveGerarRelatorioEstoqueMesmoComEstoqueVazio() {
        when(uniformeService.buscarPainelEstoque()).thenReturn(List.of());
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        byte[] resultado = relatorioService.gerarRelatorioEstoque(FormatoRelatorio.PDF);

        assertNotNull(resultado);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Estoque de Uniformes"), isNull(),
                eq(List.of("Tipo", "Tamanho", "Sexo", "Quantidade", "Limite", "Situação")), eq(List.of()),
                eq(List.of("Total em estoque: 0 unidades")));
    }

    @Test
    void deveGerarRelatorioEstoqueEmCsvQuandoFormatoForCsv() {
        when(uniformeService.buscarPainelEstoque()).thenReturn(List.of());
        when(csvTableBuilder.gerarCsv(any(), any(), any(), any(), any())).thenReturn(new byte[]{9});

        byte[] resultado = relatorioService.gerarRelatorioEstoque(FormatoRelatorio.CSV);

        assertArrayEquals(new byte[]{9}, resultado);
        verify(csvTableBuilder).gerarCsv(eq("Relatório de Estoque de Uniformes"), isNull(),
                eq(List.of("Tipo", "Tamanho", "Sexo", "Quantidade", "Limite", "Situação")), eq(List.of()), any());
        verifyNoInteractions(pdfTableBuilder);
    }

    @Test
    void deveFormatarQuantidadesComSeparadorDeMilharPtBr() {
        when(uniformeService.buscarPainelEstoque()).thenReturn(List.of(linhaEstoque(1234, 5, SituacaoEstoque.OK)));
        when(csvTableBuilder.gerarCsv(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        relatorioService.gerarRelatorioEstoque(FormatoRelatorio.CSV);

        ArgumentCaptor<List<List<String>>> linhasCaptor = ArgumentCaptor.forClass(List.class);
        verify(csvTableBuilder).gerarCsv(any(), any(), any(), linhasCaptor.capture(),
                eq(List.of("Total em estoque: 1.234 unidades")));
        assertEquals("1.234", linhasCaptor.getValue().getFirst().get(3));
    }

    private ResponseEstoquePainelDTO linhaEstoque(int quantidade, int limite, SituacaoEstoque situacao) {
        return new ResponseEstoquePainelDTO(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "Camiseta",
                Tamanho.M, Sexo.MASCULINO, quantidade, limite, situacao);
    }

    @Test
    void deveGerarRelatorioEntradaComSucesso() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 3, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 3, 31, 23, 59, 59));

        TipoUniforme tipo = TipoUniforme.builder().id(java.util.UUID.randomUUID()).tipo("Camiseta").build();
        Lote lote = Lote.builder().fornecedor("Fornecedor A").dataEntrega(LocalDateTime.of(2026, 3, 10, 8, 0)).build();
        ItemLote item = ItemLote.builder().tipoUniforme(tipo).lote(lote).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(15).build();

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of(item));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        byte[] resultado = relatorioService.gerarRelatorioEntrada(filtro, FormatoRelatorio.PDF);

        assertNotNull(resultado);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Entrada de Uniformes"), any(),
                eq(List.of("Data", "Fornecedor", "Tipo", "Tamanho", "Sexo", "Quantidade")), any(),
                eq(List.of("Total de entradas: 15 unidades")));
    }

    @Test
    void deveGerarRelatorioEntradaVazioQuandoNaoHaEntradasNoPeriodo() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 3, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 3, 31, 23, 59, 59));

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());

        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        assertNotNull(relatorioService.gerarRelatorioEntrada(filtro, FormatoRelatorio.PDF));

        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Entrada de Uniformes"), eq("Período: 01/03/2026 a 31/03/2026"),
                any(), eq(List.of()), eq(List.of("Total de entradas: 0 unidades")));
    }

    @Test
    void deveGerarRelatorioSaidaComSucesso() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2026, null, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        TipoUniforme tipo = TipoUniforme.builder().id(java.util.UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        Aluno aluno = Aluno.builder().nome("João").build();
        Pedido pedido = Pedido.builder().aluno(aluno).dataEfetivada(LocalDateTime.of(2026, 5, 20, 9, 0)).build();
        PedidoUniforme item = PedidoUniforme.builder().pedido(pedido).uniforme(uniforme).quantidade(4).build();

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of(item));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        byte[] resultado = relatorioService.gerarRelatorioSaida(filtro, FormatoRelatorio.PDF);

        assertNotNull(resultado);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Saída de Uniformes"), any(),
                eq(List.of("Data", "Aluno", "Tipo", "Tamanho", "Sexo", "Quantidade")), any(),
                eq(List.of("Total de saídas: 4 unidades")));
    }

    @Test
    void deveGerarRelatorioSaidaVazioEmCsvQuandoNaoHaSaidasNoPeriodo() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2026, null, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());

        when(csvTableBuilder.gerarCsv(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        assertNotNull(relatorioService.gerarRelatorioSaida(filtro, FormatoRelatorio.CSV));

        verify(csvTableBuilder).gerarCsv(eq("Relatório de Saída de Uniformes"), any(),
                eq(List.of("Data", "Aluno", "Tipo", "Tamanho", "Sexo", "Quantidade")), eq(List.of()),
                eq(List.of("Total de saídas: 0 unidades")));
    }

    @Test
    void deveAgruparEntregasPorTurmaComSubtotalDeCadaUma() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2026, null, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        // Fora de ordem de propósito: o relatório é que precisa agrupar.
        PedidoUniforme itemB = entregaDe("6º B", "Ana", 3, LocalDateTime.of(2026, 5, 10, 9, 0));
        PedidoUniforme itemA1 = entregaDe("6º A", "Carlos", 2, LocalDateTime.of(2026, 5, 11, 9, 0));
        PedidoUniforme itemA2 = entregaDe("6º A", "Bruna", 4, LocalDateTime.of(2026, 5, 12, 9, 0));

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim()))
                .thenReturn(List.of(itemB, itemA1, itemA2));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        byte[] resultado = relatorioService.gerarRelatorioEntregasPorTurma(filtro, null, FormatoRelatorio.PDF);

        assertNotNull(resultado);
        ArgumentCaptor<List<List<String>>> linhasCaptor = ArgumentCaptor.forClass(List.class);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Entregas por Turma"), any(),
                eq(List.of("Turma", "Aluno", "Data", "Tipo", "Tamanho", "Sexo", "Quantidade")),
                linhasCaptor.capture(),
                eq(List.of("6º A: 6 unidades", "6º B: 3 unidades", "Total de entregas: 9 unidades")));

        List<List<String>> linhas = linhasCaptor.getValue();
        assertEquals(List.of("6º A", "Bruna"), linhas.get(0).subList(0, 2));
        assertEquals(List.of("6º A", "Carlos"), linhas.get(1).subList(0, 2));
        assertEquals(List.of("6º B", "Ana"), linhas.get(2).subList(0, 2));
    }

    @Test
    void deveConsultarApenasUmaTurmaQuandoTurmaIdEInformado() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2026, null, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59, 59));
        java.util.UUID turmaId = java.util.UUID.randomUUID();

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(pedidoUniformeService.buscarItensPorPeriodoETurma(periodo.inicio(), periodo.fim(), turmaId))
                .thenReturn(List.of(entregaDe("6º A", "Bruna", 4, LocalDateTime.of(2026, 5, 12, 9, 0))));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        relatorioService.gerarRelatorioEntregasPorTurma(filtro, turmaId, FormatoRelatorio.PDF);

        verify(pedidoUniformeService).buscarItensPorPeriodoETurma(periodo.inicio(), periodo.fim(), turmaId);
        verify(pedidoUniformeService, never()).buscarItensPorPeriodo(any(), any());
    }

    @Test
    void deveGerarRelatorioDeEntregasVazioQuandoNaoHaEntregasDaTurmaNoPeriodo() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2026, null, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59, 59));
        java.util.UUID turmaId = java.util.UUID.randomUUID();

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(pedidoUniformeService.buscarItensPorPeriodoETurma(periodo.inicio(), periodo.fim(), turmaId))
                .thenReturn(List.of());

        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        assertNotNull(relatorioService.gerarRelatorioEntregasPorTurma(filtro, turmaId, FormatoRelatorio.PDF));

        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Entregas por Turma"), any(), any(), eq(List.of()),
                eq(List.of("Total de entregas: 0 unidades")));
    }

    private PedidoUniforme entregaDe(String nomeTurma, String nomeAluno, int quantidade, LocalDateTime data) {
        TipoUniforme tipo = TipoUniforme.builder().id(java.util.UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        Turma turma = Turma.builder().id(java.util.UUID.randomUUID()).nome(nomeTurma).build();
        Aluno aluno = Aluno.builder().nome(nomeAluno).turma(turma).build();
        Pedido pedido = Pedido.builder().aluno(aluno).dataEfetivada(data).build();

        return PedidoUniforme.builder().pedido(pedido).uniforme(uniforme).quantidade(quantidade).build();
    }

    @Test
    void deveGerarRelatorioTransacoesComTotaisESaldoPositivo() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 3, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 3, 31, 23, 59, 59));

        TipoUniforme tipo = TipoUniforme.builder().id(java.util.UUID.randomUUID()).tipo("Camiseta").build();
        Lote lote = Lote.builder().fornecedor("Fornecedor A").dataEntrega(LocalDateTime.of(2026, 3, 5, 8, 0)).build();
        ItemLote entrada = ItemLote.builder().tipoUniforme(tipo).lote(lote).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(20).build();

        Uniforme uniforme = Uniforme.builder().tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        Aluno aluno = Aluno.builder().nome("João").build();
        Pedido pedido = Pedido.builder().aluno(aluno).dataEfetivada(LocalDateTime.of(2026, 3, 15, 9, 0)).build();
        PedidoUniforme saida = PedidoUniforme.builder().pedido(pedido).uniforme(uniforme).quantidade(7).build();

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of(entrada));
        when(pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of(saida));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        byte[] resultado = relatorioService.gerarRelatorioTransacoes(filtro, FormatoRelatorio.PDF);

        assertNotNull(resultado);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Transações de Uniformes"), any(),
                eq(List.of("Data", "Tipo", "Descrição", "Quantidade")), any(),
                eq(List.of(
                        "Total de entradas: 20 unidades",
                        "Total de saídas: 7 unidades",
                        "Saldo do período: +13 unidades"
                )));
    }

    @Test
    void deveGerarRelatorioTransacoesComSaldoNegativo() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 3, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 3, 31, 23, 59, 59));

        TipoUniforme tipo = TipoUniforme.builder().id(java.util.UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        Aluno aluno = Aluno.builder().nome("João").build();
        Pedido pedido = Pedido.builder().aluno(aluno).dataEfetivada(LocalDateTime.of(2026, 3, 15, 9, 0)).build();
        PedidoUniforme saida = PedidoUniforme.builder().pedido(pedido).uniforme(uniforme).quantidade(10).build();

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());
        when(pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of(saida));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        relatorioService.gerarRelatorioTransacoes(filtro, FormatoRelatorio.PDF);

        verify(pdfTableBuilder).gerarPdf(any(), any(), any(), any(),
                eq(List.of(
                        "Total de entradas: 0 unidades",
                        "Total de saídas: 10 unidades",
                        "Saldo do período: -10 unidades"
                )));
    }

    @Test
    void deveGerarRelatorioDeTransacoesVazioQuandoNaoHaMovimentoNoPeriodo() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 3, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 3, 31, 23, 59, 59));

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());
        when(pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());

        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1});

        assertNotNull(relatorioService.gerarRelatorioTransacoes(filtro, FormatoRelatorio.PDF));

        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Transações de Uniformes"), any(), any(), eq(List.of()),
                eq(List.of(
                        "Total de entradas: 0 unidades",
                        "Total de saídas: 0 unidades",
                        "Saldo do período: +0 unidades"
                )));
    }
}