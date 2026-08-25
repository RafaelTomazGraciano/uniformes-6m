package com.six_m.uniform.domain.escola;

import com.six_m.uniform.domain.escola.dto.ResponseEscolaDTO;
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
@RequestMapping("api/escola")
@Tag(name = "Escola", description = "Consulta de escolas cadastradas")
@SecurityRequirement(name = "bearerAuth")
public class EscolaController {

    private final EscolaService escolaService;

    @GetMapping
    @Operation(summary = "Listar escolas", description = "Retorna uma lista paginada de escolas. Ordenação disponível por: nome, tipo, endereco (ex: sort=nome,asc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseEscolaDTO>> buscarTodasEscolas(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(escolaService.buscarTodasEscolas(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar escola por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Escola encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Escola não encontrada")
    })
    public ResponseEntity<ResponseEscolaDTO> buscarEscola(
            @Parameter(description = "Identificador da escola") @PathVariable UUID id) {
        return ResponseEntity.ok(escolaService.buscarEscola(id));
    }

}