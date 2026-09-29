package com.six_m.uniform.domain.relatorio;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeService;
import com.six_m.uniform.domain.relatorio.dto.RelatorioFiltroDTO;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.pdf.PdfTableBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UniformeService uniformeService;
    private final ItemLoteService itemLoteService;
    private final PedidoUniformeService pedidoUniformeService;
    private final RelatorioPeriodoResolver periodoResolver;
    private final PdfTableBuilder pdfTableBuilder;

    public byte[] gerarRelatorioEstoque() {
        List<Uniforme> uniformes = uniformeService.buscarTodosUniformesEntidades();

        if (uniformes.isEmpty()) {
            throw new NotFoundException("Não há uniformes cadastrados no estoque");
        }

        List<List<String>> linhas = uniformes.stream()
                .map(u -> List.of(
                        u.getTipoUniforme().getTipo(),
                        u.getTamanho().name(),
                        u.getSexo().name(),
                        String.valueOf(u.getQuantidade())
                ))
                .toList();

        return pdfTableBuilder.gerarPdf(
                "Relatório de Estoque de Uniformes",
                null,
                List.of("Tipo", "Tamanho", "Sexo", "Quantidade"),
                linhas,
                null
        );
    }

    public byte[] gerarRelatorioEntrada(RelatorioFiltroDTO filtro) {
        Periodo periodo = periodoResolver.resolver(filtro);
        List<ItemLote> itens = itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim());

        if (itens.isEmpty()) {
            throw new NotFoundException("Não há registros de entrada de uniformes no período informado");
        }

        List<List<String>> linhas = itens.stream()
                .sorted(Comparator.comparing(i -> i.getLote().getDataEntrega()))
                .map(i -> List.of(
                        i.getLote().getDataEntrega().format(FORMATO_DATA),
                        i.getLote().getFornecedor(),
                        i.getTipoUniforme().getTipo(),
                        i.getTamanho().name(),
                        i.getSexo().name(),
                        String.valueOf(i.getQuantidade())
                ))
                .toList();

        int totalEntrada = itens.stream().mapToInt(ItemLote::getQuantidade).sum();

        return pdfTableBuilder.gerarPdf(
                "Relatório de Entrada de Uniformes",
                descricaoPeriodo(periodo),
                List.of("Data", "Fornecedor", "Tipo", "Tamanho", "Sexo", "Quantidade"),
                linhas,
                List.of("Total de entradas: " + totalEntrada + " unidades")
        );
    }

    public byte[] gerarRelatorioSaida(RelatorioFiltroDTO filtro) {
        Periodo periodo = periodoResolver.resolver(filtro);
        List<PedidoUniforme> itens = pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim());

        if (itens.isEmpty()) {
            throw new NotFoundException("Não há registros de saída de uniformes no período informado");
        }

        List<List<String>> linhas = itens.stream()
                .sorted(Comparator.comparing(i -> i.getPedido().getDataEfetivada()))
                .map(i -> List.of(
                        i.getPedido().getDataEfetivada().format(FORMATO_DATA),
                        i.getPedido().getAluno().getNome(),
                        i.getUniforme().getTipoUniforme().getTipo(),
                        i.getUniforme().getTamanho().name(),
                        i.getUniforme().getSexo().name(),
                        String.valueOf(i.getQuantidade())
                ))
                .toList();

        int totalSaida = itens.stream().mapToInt(PedidoUniforme::getQuantidade).sum();

        return pdfTableBuilder.gerarPdf(
                "Relatório de Saída de Uniformes",
                descricaoPeriodo(periodo),
                List.of("Data", "Aluno", "Tipo", "Tamanho", "Sexo", "Quantidade"),
                linhas,
                List.of("Total de saídas: " + totalSaida + " unidades")
        );
    }

    public byte[] gerarRelatorioEntregasPorTurma(RelatorioFiltroDTO filtro, UUID turmaId) {
        Periodo periodo = periodoResolver.resolver(filtro);

        List<PedidoUniforme> itens = turmaId == null
                ? pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())
                : pedidoUniformeService.buscarItensPorPeriodoETurma(periodo.inicio(), periodo.fim(), turmaId);

        if (itens.isEmpty()) {
            throw new NotFoundException("Não há entregas de uniformes para a turma no período informado");
        }

        /* Turma, depois aluno, depois data: é como a secretaria confere a lista,
           varrendo uma turma inteira antes de passar para a próxima. */
        List<PedidoUniforme> ordenados = itens.stream()
                .sorted(Comparator.comparing(this::nomeDaTurma)
                        .thenComparing(i -> i.getPedido().getAluno().getNome())
                        .thenComparing(i -> i.getPedido().getDataEfetivada()))
                .toList();

        List<List<String>> linhas = ordenados.stream()
                .map(i -> List.of(
                        nomeDaTurma(i),
                        i.getPedido().getAluno().getNome(),
                        i.getPedido().getDataEfetivada().format(FORMATO_DATA),
                        i.getUniforme().getTipoUniforme().getTipo(),
                        i.getUniforme().getTamanho().name(),
                        i.getUniforme().getSexo().name(),
                        String.valueOf(i.getQuantidade())
                ))
                .toList();

        return pdfTableBuilder.gerarPdf(
                "Relatório de Entregas por Turma",
                descricaoPeriodo(periodo),
                List.of("Turma", "Aluno", "Data", "Tipo", "Tamanho", "Sexo", "Quantidade"),
                linhas,
                totaisPorTurma(ordenados)
        );
    }

    /* O PDF é uma tabela plana, então o fechamento por turma vai no rodapé:
       é o número que a coordenação lê primeiro. */
    private List<String> totaisPorTurma(List<PedidoUniforme> itens) {
        Map<String, Integer> porTurma = new LinkedHashMap<>();

        for (PedidoUniforme item : itens) {
            porTurma.merge(nomeDaTurma(item), item.getQuantidade(), Integer::sum);
        }

        List<String> totais = new ArrayList<>();
        for (Map.Entry<String, Integer> entrada : porTurma.entrySet()) {
            totais.add(entrada.getKey() + ": " + entrada.getValue() + " unidades");
        }

        int totalGeral = itens.stream().mapToInt(PedidoUniforme::getQuantidade).sum();
        totais.add("Total de entregas: " + totalGeral + " unidades");

        return totais;
    }

    private String nomeDaTurma(PedidoUniforme item) {
        return item.getPedido().getAluno().getTurma().getNome();
    }

    public byte[] gerarRelatorioTransacoes(RelatorioFiltroDTO filtro) {
        Periodo periodo = periodoResolver.resolver(filtro);
        List<ItemLote> entradas = itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim());
        List<PedidoUniforme> saidas = pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim());

        if (entradas.isEmpty() && saidas.isEmpty()) {
            throw new NotFoundException("Não há transações de uniformes no período informado");
        }

        List<TransacaoLinha> transacoes = new ArrayList<>();

        for (ItemLote item : entradas) {
            transacoes.add(new TransacaoLinha(
                    item.getLote().getDataEntrega(),
                    "ENTRADA",
                    item.getTipoUniforme().getTipo() + " (" + item.getTamanho() + "/" + item.getSexo() + ")",
                    item.getQuantidade()
            ));
        }

        for (PedidoUniforme item : saidas) {
            transacoes.add(new TransacaoLinha(
                    item.getPedido().getDataEfetivada(),
                    "SAÍDA",
                    item.getUniforme().getTipoUniforme().getTipo() + " (" + item.getUniforme().getTamanho() + "/" + item.getUniforme().getSexo() + ")",
                    item.getQuantidade()
            ));
        }

        List<List<String>> linhas = transacoes.stream()
                .sorted(Comparator.comparing(TransacaoLinha::data))
                .map(t -> List.of(
                        t.data().format(FORMATO_DATA),
                        t.tipo(),
                        t.descricao(),
                        String.valueOf(t.quantidade())
                ))
                .toList();

        int totalEntrada = entradas.stream().mapToInt(ItemLote::getQuantidade).sum();
        int totalSaida = saidas.stream().mapToInt(PedidoUniforme::getQuantidade).sum();
        int saldo = totalEntrada - totalSaida;

        return pdfTableBuilder.gerarPdf(
                "Relatório de Transações de Uniformes",
                descricaoPeriodo(periodo),
                List.of("Data", "Tipo", "Descrição", "Quantidade"),
                linhas,
                List.of(
                        "Total de entradas: " + totalEntrada + " unidades",
                        "Total de saídas: " + totalSaida + " unidades",
                        "Saldo do período: " + (saldo >= 0 ? "+" : "") + saldo + " unidades"
                )
        );
    }

    private String descricaoPeriodo(Periodo periodo) {
        return "Período: " + periodo.inicio().format(FORMATO_DATA) + " a " + periodo.fim().format(FORMATO_DATA);
    }

    private record TransacaoLinha(java.time.LocalDateTime data, String tipo, String descricao, Integer quantidade) {
    }
}