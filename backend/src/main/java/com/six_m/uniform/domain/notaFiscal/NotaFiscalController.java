package com.six_m.uniform.domain.notaFiscal;

import com.six_m.uniform.domain.notaFiscal.dto.RequestAtualizarNotaFiscalDTO;
import com.six_m.uniform.domain.notaFiscal.dto.RequestCriarNotaFiscalDTO;
import com.six_m.uniform.domain.notaFiscal.dto.ResponseNotaFiscalDTO;
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
@RequestMapping("api/nota-fiscal")
@Tag(name = "Nota Fiscal", description = "Cadastro e gestão de notas fiscais de fornecedores")
@SecurityRequirement(name = "bearerAuth")
public class NotaFiscalController {

    private final NotaFiscalService notaFiscalService;

    @PostMapping
    @Operation(summary = "Criar nota fiscal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nota fiscal criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Chave de acesso já cadastrada ou dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<ResponseNotaFiscalDTO> criarNotaFiscal(@Valid @RequestBody RequestCriarNotaFiscalDTO request) {
        ResponseNotaFiscalDTO response = notaFiscalService.criarNotaFiscal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar notas fiscais", description = "Retorna uma lista paginada de notas fiscais. Ordenação disponível por: chaveAcesso (ex: sort=chaveAcesso,asc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseNotaFiscalDTO>> buscarTodasNotasFiscais(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(notaFiscalService.buscarTodasNotasFiscais(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar nota fiscal por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nota fiscal encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada")
    })
    public ResponseEntity<ResponseNotaFiscalDTO> buscarNotaFiscal(
            @Parameter(description = "Identificador da nota fiscal") @PathVariable UUID id) {
        return ResponseEntity.ok(notaFiscalService.buscarNotaFiscal(id));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar nota fiscal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nota fiscal atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nova chave de acesso já pertence a outra nota fiscal"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada")
    })
    public ResponseEntity<ResponseNotaFiscalDTO> atualizarNotaFiscal(
            @Parameter(description = "Identificador da nota fiscal") @PathVariable UUID id,
            @Valid @RequestBody RequestAtualizarNotaFiscalDTO request) {
        return ResponseEntity.ok(notaFiscalService.atualizarNotaFiscal(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletar nota fiscal", description = "Remove uma nota fiscal. Só é possível se não houver lotes vinculados a ela")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nota fiscal deletada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Existem lotes vinculados à nota fiscal"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada")
    })
    public ResponseEntity<MessageResponseDTO> deletarNotaFiscal(
            @Parameter(description = "Identificador da nota fiscal") @PathVariable UUID id) {
        return ResponseEntity.ok(notaFiscalService.deletarNotaFiscal(id));
    }
}