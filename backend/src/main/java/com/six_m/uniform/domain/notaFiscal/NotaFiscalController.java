package com.six_m.uniform.domain.notaFiscal;

import com.six_m.uniform.domain.notaFiscal.dto.ResponseNotaFiscalDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
}