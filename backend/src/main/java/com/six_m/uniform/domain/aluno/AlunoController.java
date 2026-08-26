package com.six_m.uniform.domain.aluno;

import com.six_m.uniform.domain.aluno.dto.RequestAtualizarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.RequestCriarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.ResponseAlunoDTO;
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
@RequestMapping("api/aluno")
@Tag(name = "Aluno", description = "Cadastro e gestão de alunos")
@SecurityRequirement(name = "bearerAuth")
public class AlunoController {

    private final AlunoService alunoService;

    @PostMapping
    @Operation(summary = "Criar aluno", description = "Cadastra um novo aluno vinculado a uma turma existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aluno criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada")
    })
    public ResponseEntity<ResponseAlunoDTO> criarAluno(@Valid @RequestBody RequestCriarAlunoDTO request) {
        ResponseAlunoDTO response = alunoService.criarAluno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar alunos", description = "Retorna uma lista paginada de alunos. Ordenação disponível por: nome (ex: sort=nome,asc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseAlunoDTO>> buscarTodosAlunos(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(alunoService.buscarTodosAlunos(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar aluno por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    public ResponseEntity<ResponseAlunoDTO> buscarAluno(
            @Parameter(description = "Identificador do aluno") @PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.buscarAluno(id));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar aluno", description = "Atualiza nome e/ou turma de um aluno existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Aluno ou turma não encontrados")
    })
    public ResponseEntity<ResponseAlunoDTO> atualizarAluno(
            @Parameter(description = "Identificador do aluno") @PathVariable UUID id,
            @Valid @RequestBody RequestAtualizarAlunoDTO request) {
        return ResponseEntity.ok(alunoService.atualizarAluno(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletar aluno", description = "Remove (soft delete) um aluno")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    public ResponseEntity<MessageResponseDTO> deletarAluno(
            @Parameter(description = "Identificador do aluno") @PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.deletarAluno(id));
    }
}