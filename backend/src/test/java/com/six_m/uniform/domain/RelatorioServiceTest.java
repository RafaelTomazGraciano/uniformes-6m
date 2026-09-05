package com.six_m.uniform.domain;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.pedido.Pedido;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeService;
import com.six_m.uniform.domain.relatorio.Periodo;
import com.six_m.uniform.domain.relatorio.RelatorioPeriodoResolver;
import com.six_m.uniform.domain.relatorio.RelatorioService;
import com.six_m.uniform.domain.relatorio.dto.RelatorioFiltroDTO;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import com.six_m.uniform.shared.enums.TipoFiltroRelatorio;
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

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void deveGerarRelatorioEstoqueComSucesso() {
        TipoUniforme tipo = TipoUniforme.builder().id(java.util.UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(10).build();

        when(uniformeService.buscarTodosUniformesEntidades()).thenReturn(List.of(uniforme));
        when(pdfTableBuilder.gerarPdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1, 2, 3});

        byte[] resultado = relatorioService.gerarRelatorioEstoque();

        assertNotNull(resultado);
        ArgumentCaptor<List<List<String>>> linhasCaptor = ArgumentCaptor.forClass(List.class);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Estoque de Uniformes"), isNull(),
                eq(List.of("Tipo", "Tamanho", "Sexo", "Quantidade")), linhasCaptor.capture(), isNull());

        assertEquals(List.of("Camiseta", "M", "MASCULINO", "10"), linhasCaptor.getValue().getFirst());
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueVazio() {
        when(uniformeService.buscarTodosUniformesEntidades()).thenReturn(List.of());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> relatorioService.gerarRelatorioEstoque());

        assertEquals("Não há uniformes cadastrados no estoque", exception.getMessage());
        verify(pdfTableBuilder, never()).gerarPdf(any(), any(), any(), any(), any());
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

        byte[] resultado = relatorioService.gerarRelatorioEntrada(filtro);

        assertNotNull(resultado);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Entrada de Uniformes"), any(),
                eq(List.of("Data", "Fornecedor", "Tipo", "Tamanho", "Sexo", "Quantidade")), any(),
                eq(List.of("Total de entradas: 15 unidades")));
    }

    @Test
    void deveLancarExcecaoQuandoNaoHaEntradasNoPeriodo() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 3, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 3, 31, 23, 59, 59));

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> relatorioService.gerarRelatorioEntrada(filtro));

        assertEquals("Não há registros de entrada de uniformes no período informado", exception.getMessage());
        verify(pdfTableBuilder, never()).gerarPdf(any(), any(), any(), any(), any());
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

        byte[] resultado = relatorioService.gerarRelatorioSaida(filtro);

        assertNotNull(resultado);
        verify(pdfTableBuilder).gerarPdf(eq("Relatório de Saída de Uniformes"), any(),
                eq(List.of("Data", "Aluno", "Tipo", "Tamanho", "Sexo", "Quantidade")), any(),
                eq(List.of("Total de saídas: 4 unidades")));
    }

    @Test
    void deveLancarExcecaoQuandoNaoHaSaidasNoPeriodo() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2026, null, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> relatorioService.gerarRelatorioSaida(filtro));

        verify(pdfTableBuilder, never()).gerarPdf(any(), any(), any(), any(), any());
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

        byte[] resultado = relatorioService.gerarRelatorioTransacoes(filtro);

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

        relatorioService.gerarRelatorioTransacoes(filtro);

        verify(pdfTableBuilder).gerarPdf(any(), any(), any(), any(),
                eq(List.of(
                        "Total de entradas: 0 unidades",
                        "Total de saídas: 10 unidades",
                        "Saldo do período: -10 unidades"
                )));
    }

    @Test
    void deveLancarExcecaoQuandoNaoHaEntradasNemSaidasNoPeriodoParaTransacoes() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 3, null, null);
        Periodo periodo = new Periodo(LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 3, 31, 23, 59, 59));

        when(periodoResolver.resolver(filtro)).thenReturn(periodo);
        when(itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());
        when(pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())).thenReturn(List.of());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> relatorioService.gerarRelatorioTransacoes(filtro));

        assertEquals("Não há transações de uniformes no período informado", exception.getMessage());
        verify(pdfTableBuilder, never()).gerarPdf(any(), any(), any(), any(), any());
    }
}