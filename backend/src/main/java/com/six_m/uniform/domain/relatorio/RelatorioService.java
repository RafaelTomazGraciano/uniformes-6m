package com.six_m.uniform.domain.relatorio;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeService;
import com.six_m.uniform.domain.relatorio.dto.RelatorioFiltroDTO;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.domain.uniforme.dto.ResponseEstoquePainelDTO;
import com.six_m.uniform.shared.csv.CsvTableBuilder;
import com.six_m.uniform.shared.pdf.PdfTableBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final UniformeService uniformeService;
    private final ItemLoteService itemLoteService;
    private final PedidoUniformeService pedidoUniformeService;
    private final RelatorioPeriodoResolver periodoResolver;
    private final PdfTableBuilder pdfTableBuilder;
    private final CsvTableBuilder csvTableBuilder;

    /* Lê a view vw_estoque, que já traz a situação calculada. Estoque vazio não é
       erro: o documento sai com o cabeçalho e a indicação de que não há registros. */
    public byte[] gerarRelatorioEstoque(FormatoRelatorio formato) {
        List<ResponseEstoquePainelDTO> estoque = uniformeService.buscarPainelEstoque();

        List<List<String>> linhas = estoque.stream()
                .map(e -> List.of(
                        e.tipoUniformeNome(),
                        e.tamanho().name(),
                        rotulo(e.sexo()),
                        numero(e.quantidade()),
                        numero(e.estoqueBaixo()),
                        rotulo(e.situacao())
                ))
                .toList();

        int totalPecas = estoque.stream().mapToInt(ResponseEstoquePainelDTO::quantidade).sum();

        return gerar(formato,
                "Relatório de Estoque de Uniformes",
                null,
                List.of("Tipo", "Tamanho", "Sexo", "Quantidade", "Limite", "Situação"),
                linhas,
                List.of("Total em estoque: " + unidades(totalPecas))
        );
    }

    public byte[] gerarRelatorioEntrada(RelatorioFiltroDTO filtro, FormatoRelatorio formato) {
        Periodo periodo = periodoResolver.resolver(filtro);
        List<ItemLote> itens = itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim());


        List<List<String>> linhas = itens.stream()
                .sorted(Comparator.comparing(i -> i.getLote().getDataEntrega()))
                .map(i -> List.of(
                        i.getLote().getDataEntrega().format(FORMATO_DATA),
                        i.getLote().getFornecedor(),
                        i.getTipoUniforme().getTipo(),
                        i.getTamanho().name(),
                        rotulo(i.getSexo()),
                        numero(i.getQuantidade())
                ))
                .toList();

        int totalEntrada = itens.stream().mapToInt(ItemLote::getQuantidade).sum();

        return gerar(formato,
                "Relatório de Entrada de Uniformes",
                descricaoPeriodo(periodo),
                List.of("Data", "Fornecedor", "Tipo", "Tamanho", "Sexo", "Quantidade"),
                linhas,
                List.of("Total de entradas: " + unidades(totalEntrada))
        );
    }

    public byte[] gerarRelatorioSaida(RelatorioFiltroDTO filtro, FormatoRelatorio formato) {
        Periodo periodo = periodoResolver.resolver(filtro);
        List<PedidoUniforme> itens = pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim());


        List<List<String>> linhas = itens.stream()
                .sorted(Comparator.comparing(i -> i.getPedido().getDataEfetivada()))
                .map(i -> List.of(
                        i.getPedido().getDataEfetivada().format(FORMATO_DATA),
                        nomeDoAluno(i),
                        i.getUniforme().getTipoUniforme().getTipo(),
                        i.getUniforme().getTamanho().name(),
                        rotulo(i.getUniforme().getSexo()),
                        numero(i.getQuantidade())
                ))
                .toList();

        int totalSaida = itens.stream().mapToInt(PedidoUniforme::getQuantidade).sum();

        return gerar(formato,
                "Relatório de Saída de Uniformes",
                descricaoPeriodo(periodo),
                List.of("Data", "Aluno", "Tipo", "Tamanho", "Sexo", "Quantidade"),
                linhas,
                List.of("Total de saídas: " + unidades(totalSaida))
        );
    }

    public byte[] gerarRelatorioEntregasPorTurma(RelatorioFiltroDTO filtro, UUID turmaId, FormatoRelatorio formato) {
        Periodo periodo = periodoResolver.resolver(filtro);

        List<PedidoUniforme> itens = turmaId == null
                ? pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim())
                : pedidoUniformeService.buscarItensPorPeriodoETurma(periodo.inicio(), periodo.fim(), turmaId);


        /* Turma, depois aluno, depois data: é como a secretaria confere a lista,
           varrendo uma turma inteira antes de passar para a próxima. */
        List<PedidoUniforme> ordenados = itens.stream()
                .sorted(Comparator.comparing(this::nomeDaTurma)
                        .thenComparing(i -> nomeDoAluno(i))
                        .thenComparing(i -> i.getPedido().getDataEfetivada()))
                .toList();

        List<List<String>> linhas = ordenados.stream()
                .map(i -> List.of(
                        nomeDaTurma(i),
                        nomeDoAluno(i),
                        i.getPedido().getDataEfetivada().format(FORMATO_DATA),
                        i.getUniforme().getTipoUniforme().getTipo(),
                        i.getUniforme().getTamanho().name(),
                        rotulo(i.getUniforme().getSexo()),
                        numero(i.getQuantidade())
                ))
                .toList();

        return gerar(formato,
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
            totais.add(entrada.getKey() + ": " + unidades(entrada.getValue()));
        }

        int totalGeral = itens.stream().mapToInt(PedidoUniforme::getQuantidade).sum();
        totais.add("Total de entregas: " + unidades(totalGeral));

        return totais;
    }

    /* O aluno pode não carregar (registro antigo excluído); o relatório de
       prestação de contas não pode cair por causa de uma linha. */
    private String nomeDoAluno(PedidoUniforme item) {
        var aluno = item.getPedido().getAluno();
        return aluno != null ? aluno.getNome() : "Aluno removido";
    }

    private String nomeDaTurma(PedidoUniforme item) {
        var aluno = item.getPedido().getAluno();
        return aluno != null && aluno.getTurma() != null ? aluno.getTurma().getNome() : "Sem turma";
    }

    public byte[] gerarRelatorioTransacoes(RelatorioFiltroDTO filtro, FormatoRelatorio formato) {
        Periodo periodo = periodoResolver.resolver(filtro);
        List<ItemLote> entradas = itemLoteService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim());
        List<PedidoUniforme> saidas = pedidoUniformeService.buscarItensPorPeriodo(periodo.inicio(), periodo.fim());


        List<TransacaoLinha> transacoes = new ArrayList<>();

        for (ItemLote item : entradas) {
            transacoes.add(new TransacaoLinha(
                    item.getLote().getDataEntrega(),
                    "ENTRADA",
                    item.getTipoUniforme().getTipo() + " (" + item.getTamanho() + "/" + rotulo(item.getSexo()) + ")",
                    item.getQuantidade()
            ));
        }

        for (PedidoUniforme item : saidas) {
            transacoes.add(new TransacaoLinha(
                    item.getPedido().getDataEfetivada(),
                    "SAÍDA",
                    item.getUniforme().getTipoUniforme().getTipo() + " (" + item.getUniforme().getTamanho() + "/" + rotulo(item.getUniforme().getSexo()) + ")",
                    item.getQuantidade()
            ));
        }

        List<List<String>> linhas = transacoes.stream()
                .sorted(Comparator.comparing(TransacaoLinha::data))
                .map(t -> List.of(
                        t.data().format(FORMATO_DATA),
                        t.tipo(),
                        t.descricao(),
                        numero(t.quantidade())
                ))
                .toList();

        int totalEntrada = entradas.stream().mapToInt(ItemLote::getQuantidade).sum();
        int totalSaida = saidas.stream().mapToInt(PedidoUniforme::getQuantidade).sum();
        int saldo = totalEntrada - totalSaida;

        return gerar(formato,
                "Relatório de Transações de Uniformes",
                descricaoPeriodo(periodo),
                List.of("Data", "Tipo", "Descrição", "Quantidade"),
                linhas,
                List.of(
                        "Total de entradas: " + unidades(totalEntrada),
                        "Total de saídas: " + unidades(totalSaida),
                        "Saldo do período: " + (saldo >= 0 ? "+" : "") + unidades(saldo)
                )
        );
    }

    /* Os dois formatos recebem a mesma tabela: o CSV é o PDF que abre no Excel. */
    private byte[] gerar(FormatoRelatorio formato, String titulo, String descricaoPeriodo, List<String> cabecalhos,
                         List<List<String>> linhas, List<String> totais) {
        return switch (formato) {
            case PDF -> pdfTableBuilder.gerarPdf(titulo, descricaoPeriodo, cabecalhos, linhas, totais);
            case CSV -> csvTableBuilder.gerarCsv(titulo, descricaoPeriodo, cabecalhos, linhas, totais);
        };
    }

    /** Inteiro com separador de milhar pt-BR ("1.234"), que o Excel em português lê como número. */
    private String numero(int valor) {
        return String.format(PT_BR, "%,d", valor);
    }

    private String unidades(int valor) {
        return numero(valor) + " unidades";
    }

    private String descricaoPeriodo(Periodo periodo) {
        return "Período: " + periodo.inicio().format(FORMATO_DATA) + " a " + periodo.fim().format(FORMATO_DATA);
    }

    /* Relatório é lido por gente (e pelo Excel): rótulos da tela, não o nome do enum. */
    static String rotulo(com.six_m.uniform.shared.enums.Sexo sexo) {
        return sexo == com.six_m.uniform.shared.enums.Sexo.MASCULINO ? "Masculino" : "Feminino";
    }

    static String rotulo(com.six_m.uniform.domain.uniforme.SituacaoEstoque situacao) {
        return switch (situacao) {
            case ESGOTADO -> "Esgotado";
            case BAIXO -> "Baixo";
            case OK -> "OK";
        };
    }

    private record TransacaoLinha(LocalDateTime data, String tipo, String descricao, Integer quantidade) {
    }
}