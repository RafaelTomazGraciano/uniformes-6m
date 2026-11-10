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
    @Operation(summary = "Listar alunos", description = "Retorna uma lista paginada de alunos, com filtros opcionais por nome (ou nome da turma), turma e situação. Ordenação disponível por: nome (ex: sort=nome,asc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseAlunoDTO>> buscarTodosAlunos(
            @Parameter(description = "Trecho do nome do aluno ou da turma, sem diferenciar maiúsculas nem acentos") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtra os alunos de uma turma") @RequestParam(required = false) UUID turmaId,
            @Parameter(description = "true: só ativos; false: só os que saíram; ausente: todos") @RequestParam(required = false) Boolean ativo,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(alunoService.buscarTodosAlunos(nome, turmaId, ativo, pageable));
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
    @Operation(summary = "Atualizar aluno", description = "Atualiza nome, dados do responsável e/ou turma de um aluno existente")
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

    @PatchMapping("{id}/saida")
    @Operation(summary = "Registrar saída do aluno", description = "Desliga o aluno (ativo=false, dataSaida=agora). O cadastro e o histórico de entregas são mantidos, mas o aluno não pode receber novas entregas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saída registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "A saída deste aluno já foi registrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    public ResponseEntity<ResponseAlunoDTO> registrarSaida(
            @Parameter(description = "Identificador do aluno") @PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.registrarSaida(id));
    }

    @PatchMapping("{id}/reativar")
    @Operation(summary = "Reativar aluno", description = "Desfaz a saída do aluno, que volta a poder receber entregas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno reativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "O aluno já está ativo"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    public ResponseEntity<ResponseAlunoDTO> reativarAluno(
            @Parameter(description = "Identificador do aluno") @PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.reativarAluno(id));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletar aluno", description = "Remove um aluno cadastrado por engano. Recusado quando o aluno já tem entregas registradas: nesse caso, use a saída para manter o histórico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "O aluno tem entregas registradas"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    public ResponseEntity<MessageResponseDTO> deletarAluno(
            @Parameter(description = "Identificador do aluno") @PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.deletarAluno(id));
    }
}
