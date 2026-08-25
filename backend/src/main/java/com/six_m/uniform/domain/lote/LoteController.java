package com.six_m.uniform.domain.lote;

import com.six_m.uniform.domain.lote.dto.RequestAtualizarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestCriarLoteDTO;
import com.six_m.uniform.domain.lote.dto.ResponseLoteDTO;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/lote")
@Tag(name = "Lote", description = "Cadastro e gestão de lotes de entrega recebidos de fornecedores")
@SecurityRequirement(name = "bearerAuth")
public class LoteController {

    private final LoteService loteService;

    @PostMapping
    @Operation(summary = "Criar lote", description = "Cadastra um novo lote vinculado a uma nota fiscal existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lote criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada")
    })
    public ResponseEntity<ResponseLoteDTO> criarLote(@Valid @RequestBody RequestCriarLoteDTO request) {
        ResponseLoteDTO response = loteService.criarLote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar lotes", description = "Retorna uma lista paginada de lotes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseLoteDTO>> buscarTodosLotes(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(loteService.buscarTodosLotes(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar lote por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<ResponseLoteDTO> buscarLote(
            @Parameter(description = "Identificador do lote") @PathVariable UUID id) {
        return ResponseEntity.ok(loteService.buscarLote(id));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Lote ou nota fiscal não encontrados")
    })
    public ResponseEntity<ResponseLoteDTO> atualizarLote(
            @Parameter(description = "Identificador do lote") @PathVariable UUID id,
            @Valid @RequestBody RequestAtualizarLoteDTO request) {
        return ResponseEntity.ok(loteService.atualizarLote(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletar lote", description = "Remove um lote. Só é possível se não houver itens vinculados a ele")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Existem itens vinculados ao lote"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<MessageResponseDTO> deletarLote(
            @Parameter(description = "Identificador do lote") @PathVariable UUID id) {
        return ResponseEntity.ok(loteService.deletarLote(id));
    }
}