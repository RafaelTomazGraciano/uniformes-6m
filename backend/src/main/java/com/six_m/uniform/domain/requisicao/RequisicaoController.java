package com.six_m.uniform.domain.requisicao;

import com.six_m.uniform.domain.requisicao.dto.RequestCriarRequisicaoDTO;
import com.six_m.uniform.domain.requisicao.dto.ResponseRequisicaoDTO;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.shared.enums.StatusRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/requisicao")
@Tag(name = "Requisição", description = "Solicitação de novas peças de uniforme ao fornecedor. Não altera o estoque: ele só sobe com a entrada de um lote")
@SecurityRequirement(name = "bearerAuth")
public class RequisicaoController {

    private final RequisicaoService requisicaoService;

    @PostMapping
    @Operation(summary = "Criar requisição", description = "Registra uma solicitação de novas peças com status PENDENTE. O solicitante é identificado pelo token, não pelo body")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Requisição criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Tipo de uniforme não encontrado")
    })
    public ResponseEntity<ResponseRequisicaoDTO> criarRequisicao(
            @Valid @RequestBody RequestCriarRequisicaoDTO request,
            @AuthenticationPrincipal(expression = "usuario") Usuario usuario) {
        ResponseRequisicaoDTO response = requisicaoService.criarRequisicao(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar requisições", description = "Retorna uma lista paginada de requisições, opcionalmente filtrada por um ou mais status (ex: status=ATENDIDA&status=CANCELADA). Ordenação disponível por: dataCriacao, dataConclusao, quantidade (ex: sort=dataCriacao,desc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status ou parâmetro de ordenação inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseRequisicaoDTO>> buscarTodasRequisicoes(
            @Parameter(description = "Filtra pelo status da requisição (PENDENTE = em andamento)") @RequestParam(required = false) List<StatusRequisicao> status,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(requisicaoService.buscarTodasRequisicoes(status, pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar requisição por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requisição encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Requisição não encontrada")
    })
    public ResponseEntity<ResponseRequisicaoDTO> buscarRequisicao(
            @Parameter(description = "Identificador da requisição") @PathVariable UUID id) {
        return ResponseEntity.ok(requisicaoService.buscarRequisicao(id));
    }

    @PatchMapping("{id}/atender")
    @Operation(summary = "Marcar requisição como atendida", description = "Só vale para requisições pendentes. Não altera o estoque: a entrada das peças é registrada pelo lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requisição marcada como atendida"),
            @ApiResponse(responseCode = "400", description = "A requisição não está pendente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Requisição não encontrada")
    })
    public ResponseEntity<ResponseRequisicaoDTO> atenderRequisicao(
            @Parameter(description = "Identificador da requisição") @PathVariable UUID id) {
        return ResponseEntity.ok(requisicaoService.atenderRequisicao(id));
    }

    @PatchMapping("{id}/cancelar")
    @Operation(summary = "Cancelar requisição", description = "Só vale para requisições pendentes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requisição cancelada"),
            @ApiResponse(responseCode = "400", description = "A requisição não está pendente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Requisição não encontrada")
    })
    public ResponseEntity<ResponseRequisicaoDTO> cancelarRequisicao(
            @Parameter(description = "Identificador da requisição") @PathVariable UUID id) {
        return ResponseEntity.ok(requisicaoService.cancelarRequisicao(id));
    }
}
