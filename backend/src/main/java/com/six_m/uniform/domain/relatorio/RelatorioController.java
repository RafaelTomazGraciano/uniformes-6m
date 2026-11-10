package com.six_m.uniform.domain.relatorio;

import com.six_m.uniform.domain.relatorio.dto.RelatorioFiltroDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/relatorio")
@Tag(name = "Relatório", description = "Geração de relatórios em PDF ou CSV (Excel) sobre estoque, entrada, saída, entregas por turma e transações de uniformes")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class RelatorioController {

    private static final String DESCRICAO_FORMATO = "Formato do arquivo: pdf (padrão) ou csv (UTF-8 com BOM, separado por ';', abre direto no Excel)";

    private final RelatorioService relatorioService;

    @GetMapping("/estoque")
    @Operation(summary = "Relatório de estoque", description = "Gera um PDF (ou CSV, com formato=csv) com a situação atual do estoque de uniformes. Não aceita filtro de período — sempre reflete os dados atuais")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado com sucesso — sem registros, o documento sai com o cabeçalho e a indicação \"Nenhum registro encontrado\""),
            @ApiResponse(responseCode = "400", description = "Formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<byte[]> gerarRelatorioEstoque(
            @Parameter(description = DESCRICAO_FORMATO, schema = @Schema(allowableValues = {"pdf", "csv"}, defaultValue = "pdf"))
            @RequestParam(required = false) String formato) {
        FormatoRelatorio formatoRelatorio = FormatoRelatorio.de(formato);
        return responder(relatorioService.gerarRelatorioEstoque(formatoRelatorio), "relatorio-estoque", formatoRelatorio);
    }

    @GetMapping("/entrada")
    @Operation(summary = "Relatório de entrada", description = "Gera um PDF (ou CSV, com formato=csv) com as entradas de uniformes (via lotes) no período filtrado. Filtro por mês (tipo=MES, anoInicio, mesInicio, e opcionalmente anoFim/mesFim para um intervalo) ou por ano (tipo=ANO, anoInicio, e opcionalmente anoFim)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado com sucesso — sem registros, o documento sai com o cabeçalho e a indicação \"Nenhum registro encontrado\""),
            @ApiResponse(responseCode = "400", description = "Filtro ou formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<byte[]> gerarRelatorioEntrada(
            @Valid RelatorioFiltroDTO filtro,
            @Parameter(description = DESCRICAO_FORMATO, schema = @Schema(allowableValues = {"pdf", "csv"}, defaultValue = "pdf"))
            @RequestParam(required = false) String formato) {
        FormatoRelatorio formatoRelatorio = FormatoRelatorio.de(formato);
        return responder(relatorioService.gerarRelatorioEntrada(filtro, formatoRelatorio), "relatorio-entrada", formatoRelatorio);
    }

    @GetMapping("/saida")
    @Operation(summary = "Relatório de saída", description = "Gera um PDF (ou CSV, com formato=csv) com as saídas de uniformes (via pedidos) no período filtrado. Filtro por mês (tipo=MES, anoInicio, mesInicio, e opcionalmente anoFim/mesFim para um intervalo) ou por ano (tipo=ANO, anoInicio, e opcionalmente anoFim)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado com sucesso — sem registros, o documento sai com o cabeçalho e a indicação \"Nenhum registro encontrado\""),
            @ApiResponse(responseCode = "400", description = "Filtro ou formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<byte[]> gerarRelatorioSaida(
            @Valid RelatorioFiltroDTO filtro,
            @Parameter(description = DESCRICAO_FORMATO, schema = @Schema(allowableValues = {"pdf", "csv"}, defaultValue = "pdf"))
            @RequestParam(required = false) String formato) {
        FormatoRelatorio formatoRelatorio = FormatoRelatorio.de(formato);
        return responder(relatorioService.gerarRelatorioSaida(filtro, formatoRelatorio), "relatorio-saida", formatoRelatorio);
    }

    @GetMapping("/entregas-turma")
    @Operation(summary = "Relatório de entregas por turma", description = "Gera um PDF (ou CSV, com formato=csv) com os uniformes entregues aos alunos no período, agrupados por turma e com subtotal de cada uma. Informe turmaId para restringir a uma única turma; sem ele, o relatório cobre todas. Filtro por mês (tipo=MES, anoInicio, mesInicio, e opcionalmente anoFim/mesFim para um intervalo) ou por ano (tipo=ANO, anoInicio, e opcionalmente anoFim)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado com sucesso — sem registros, o documento sai com o cabeçalho e a indicação \"Nenhum registro encontrado\""),
            @ApiResponse(responseCode = "400", description = "Filtro ou formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<byte[]> gerarRelatorioEntregasPorTurma(
            @Valid RelatorioFiltroDTO filtro,
            @RequestParam(required = false) UUID turmaId,
            @Parameter(description = DESCRICAO_FORMATO, schema = @Schema(allowableValues = {"pdf", "csv"}, defaultValue = "pdf"))
            @RequestParam(required = false) String formato) {
        FormatoRelatorio formatoRelatorio = FormatoRelatorio.de(formato);
        return responder(relatorioService.gerarRelatorioEntregasPorTurma(filtro, turmaId, formatoRelatorio),
                "relatorio-entregas-turma", formatoRelatorio);
    }

    @GetMapping("/transacoes")
    @Operation(summary = "Relatório de transações", description = "Gera um PDF (ou CSV, com formato=csv) com entradas e saídas de uniformes juntas no período filtrado, incluindo total de entradas, total de saídas e saldo. Filtro por mês (tipo=MES, anoInicio, mesInicio, e opcionalmente anoFim/mesFim para um intervalo) ou por ano (tipo=ANO, anoInicio, e opcionalmente anoFim)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado com sucesso — sem registros, o documento sai com o cabeçalho e a indicação \"Nenhum registro encontrado\""),
            @ApiResponse(responseCode = "400", description = "Filtro ou formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<byte[]> gerarRelatorioTransacoes(
            @Valid RelatorioFiltroDTO filtro,
            @Parameter(description = DESCRICAO_FORMATO, schema = @Schema(allowableValues = {"pdf", "csv"}, defaultValue = "pdf"))
            @RequestParam(required = false) String formato) {
        FormatoRelatorio formatoRelatorio = FormatoRelatorio.de(formato);
        return responder(relatorioService.gerarRelatorioTransacoes(filtro, formatoRelatorio), "relatorio-transacoes", formatoRelatorio);
    }

    private ResponseEntity<byte[]> responder(byte[] conteudo, String nomeBase, FormatoRelatorio formato) {
        return ResponseEntity.ok()
                .contentType(formato.mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeBase + "." + formato.extensao())
                .body(conteudo);
    }
}