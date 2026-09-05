package com.six_m.uniform.domain.relatorio;

import com.six_m.uniform.domain.relatorio.dto.RelatorioFiltroDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/relatorio")
@Tag(name = "Relatório", description = "Geração de relatórios em PDF sobre estoque, entrada, saída e transações de uniformes")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/estoque")
    @Operation(summary = "Relatório de estoque", description = "Gera um PDF com a situação atual do estoque de uniformes. Não aceita filtro de período — sempre reflete os dados atuais")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Não há uniformes cadastrados no estoque")
    })
    public ResponseEntity<byte[]> gerarRelatorioEstoque() {
        byte[] pdf = relatorioService.gerarRelatorioEstoque();
        return responderComPdf(pdf, "relatorio-estoque.pdf");
    }

    @GetMapping("/entrada")
    @Operation(summary = "Relatório de entrada", description = "Gera um PDF com as entradas de uniformes (via lotes) no período filtrado. Filtro por mês (tipo=MES, anoInicio, mesInicio, e opcionalmente anoFim/mesFim para um intervalo) ou por ano (tipo=ANO, anoInicio, e opcionalmente anoFim)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Não há registros de entrada no período informado")
    })
    public ResponseEntity<byte[]> gerarRelatorioEntrada(@Valid RelatorioFiltroDTO filtro) {
        byte[] pdf = relatorioService.gerarRelatorioEntrada(filtro);
        return responderComPdf(pdf, "relatorio-entrada.pdf");
    }

    @GetMapping("/saida")
    @Operation(summary = "Relatório de saída", description = "Gera um PDF com as saídas de uniformes (via pedidos) no período filtrado. Filtro por mês (tipo=MES, anoInicio, mesInicio, e opcionalmente anoFim/mesFim para um intervalo) ou por ano (tipo=ANO, anoInicio, e opcionalmente anoFim)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Não há registros de saída no período informado")
    })
    public ResponseEntity<byte[]> gerarRelatorioSaida(@Valid RelatorioFiltroDTO filtro) {
        byte[] pdf = relatorioService.gerarRelatorioSaida(filtro);
        return responderComPdf(pdf, "relatorio-saida.pdf");
    }

    @GetMapping("/transacoes")
    @Operation(summary = "Relatório de transações", description = "Gera um PDF com entradas e saídas de uniformes juntas no período filtrado, incluindo total de entradas, total de saídas e saldo. Filtro por mês (tipo=MES, anoInicio, mesInicio, e opcionalmente anoFim/mesFim para um intervalo) ou por ano (tipo=ANO, anoInicio, e opcionalmente anoFim)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Não há transações no período informado")
    })
    public ResponseEntity<byte[]> gerarRelatorioTransacoes(@Valid RelatorioFiltroDTO filtro) {
        byte[] pdf = relatorioService.gerarRelatorioTransacoes(filtro);
        return responderComPdf(pdf, "relatorio-transacoes.pdf");
    }

    private ResponseEntity<byte[]> responderComPdf(byte[] pdf, String nomeArquivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeArquivo)
                .body(pdf);
    }
}