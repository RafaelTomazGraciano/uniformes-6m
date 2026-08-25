package com.six_m.uniform.domain.tipoUniforme;

import com.six_m.uniform.domain.tipoUniforme.dto.RequestAtualizarTipoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.dto.RequestCriarTipoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.dto.ResponseTipoUniformeDTO;
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
@RequestMapping("api/tipo-uniforme")
@Tag(name = "Tipo de Uniforme", description = "Cadastro e gestão dos tipos de uniforme (ex: camiseta, calça)")
@SecurityRequirement(name = "bearerAuth")
public class TipoUniformeController {

    private final TipoUniformeService tipoUniformeService;

    @PostMapping
    @Operation(summary = "Criar tipo de uniforme")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tipo de uniforme criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<ResponseTipoUniformeDTO> criarTipoUniforme(@Valid @RequestBody RequestCriarTipoUniformeDTO request) {
        ResponseTipoUniformeDTO response = tipoUniformeService.criarTipoUniforme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar tipos de uniforme", description = "Retorna uma lista paginada de tipos de uniforme. Ordenação disponível por: tipo (ex: sort=tipo,asc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseTipoUniformeDTO>> buscarTodosTiposUniforme(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(tipoUniformeService.buscarTodosTiposUniforme(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar tipo de uniforme por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo de uniforme encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Tipo de uniforme não encontrado")
    })
    public ResponseEntity<ResponseTipoUniformeDTO> buscarTipoUniforme(
            @Parameter(description = "Identificador do tipo de uniforme") @PathVariable UUID id) {
        return ResponseEntity.ok(tipoUniformeService.buscarTipoUniforme(id));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar tipo de uniforme")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo de uniforme atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Tipo de uniforme não encontrado")
    })
    public ResponseEntity<ResponseTipoUniformeDTO> atualizarTipoUniforme(
            @Parameter(description = "Identificador do tipo de uniforme") @PathVariable UUID id,
            @Valid @RequestBody RequestAtualizarTipoUniformeDTO request) {
        return ResponseEntity.ok(tipoUniformeService.atualizarTipoUniforme(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletar tipo de uniforme", description = "Remove um tipo de uniforme. Só é possível se não houver uniformes ou itens de lote vinculados a ele")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo de uniforme deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Existem uniformes ou itens de lote vinculados"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Tipo de uniforme não encontrado")
    })
    public ResponseEntity<MessageResponseDTO> deletarTipoUniforme(
            @Parameter(description = "Identificador do tipo de uniforme") @PathVariable UUID id) {
        return ResponseEntity.ok(tipoUniformeService.deletarTipoUniforme(id));
    }
}