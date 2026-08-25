package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.domain.uniforme.dto.RequestAtualizarUniformeDTO;
import com.six_m.uniform.domain.uniforme.dto.RequestCriarUniformeDTO;
import com.six_m.uniform.domain.uniforme.dto.ResponseUniformeDTO;
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
@RequestMapping("api/uniforme")
@Tag(name = "Uniforme", description = "Cadastro e gestão do estoque de uniformes")
@SecurityRequirement(name = "bearerAuth")
public class UniformeController {

    private final UniformeService uniformeService;

    @PostMapping
    @Operation(summary = "Criar uniforme", description = "Cadastra um novo uniforme vinculado a um tipo de uniforme existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Uniforme criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Tipo de uniforme não encontrado")
    })
    public ResponseEntity<ResponseUniformeDTO> criarUniforme(@Valid @RequestBody RequestCriarUniformeDTO request) {
        ResponseUniformeDTO response = uniformeService.criarUniforme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar uniformes", description = "Retorna uma lista paginada de uniformes. Ordenação disponível por: tamanho, quantidade, sexo (ex: sort=quantidade,desc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseUniformeDTO>> buscarTodosUniformes(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(uniformeService.buscarTodosUniformes(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar uniforme por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uniforme encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Uniforme não encontrado")
    })
    public ResponseEntity<ResponseUniformeDTO> buscarUniforme(
            @Parameter(description = "Identificador do uniforme") @PathVariable UUID id) {
        return ResponseEntity.ok(uniformeService.buscarUniforme(id));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar uniforme", description = "Atualiza tipo, tamanho, quantidade e sexo de um uniforme. Não altera o campo devolvido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uniforme atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Uniforme ou tipo de uniforme não encontrados")
    })
    public ResponseEntity<ResponseUniformeDTO> atualizarUniforme(
            @Parameter(description = "Identificador do uniforme") @PathVariable UUID id,
            @Valid @RequestBody RequestAtualizarUniformeDTO request) {
        return ResponseEntity.ok(uniformeService.atualizarUniforme(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletar uniforme", description = "Remove (soft delete) um uniforme")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uniforme deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Uniforme não encontrado")
    })
    public ResponseEntity<MessageResponseDTO> deletarUniforme(
            @Parameter(description = "Identificador do uniforme") @PathVariable UUID id) {
        return ResponseEntity.ok(uniformeService.deletarUniforme(id));
    }
}