package com.six_m.uniform.domain.turma;

import com.six_m.uniform.domain.turma.dto.RequestAtualizarTurmaDTO;
import com.six_m.uniform.domain.turma.dto.RequestCriarTurmaDTO;
import com.six_m.uniform.domain.turma.dto.ResponseTurmaDTO;
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
@RequestMapping("api/turma")
@Tag(name = "Turma", description = "Cadastro e gestão de turmas escolares")
@SecurityRequirement(name = "bearerAuth")
public class TurmaController {

    private final TurmaService turmaService;

    @PostMapping
    @Operation(summary = "Criar turma", description = "Cadastra uma nova turma")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turma criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<ResponseTurmaDTO> criarTurma(@Valid @RequestBody RequestCriarTurmaDTO request) {
        ResponseTurmaDTO response = turmaService.criarTurma(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar turmas", description = "Retorna uma lista paginada de turmas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseTurmaDTO>> buscarTodasTurmas(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(turmaService.buscarTodasTurmas(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar turma por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada")
    })
    public ResponseEntity<ResponseTurmaDTO> buscarTurma(
            @Parameter(description = "Identificador da turma") @PathVariable UUID id) {
        return ResponseEntity.ok(turmaService.buscarTurma(id));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar turma", description = "Atualiza nome, turno e ensino de uma turma existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada")
    })
    public ResponseEntity<ResponseTurmaDTO> atualizarTurma(
            @Parameter(description = "Identificador da turma") @PathVariable UUID id,
            @Valid @RequestBody RequestAtualizarTurmaDTO request) {
        return ResponseEntity.ok(turmaService.atualizarTurma(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletar turma", description = "Remove uma turma. Só é possível se não houver alunos vinculados a ela")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma deletada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Existem alunos vinculados à turma"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada")
    })
    public ResponseEntity<MessageResponseDTO> deletarTurma(
            @Parameter(description = "Identificador da turma") @PathVariable UUID id) {
        return ResponseEntity.ok(turmaService.deletarTurma(id));
    }
}