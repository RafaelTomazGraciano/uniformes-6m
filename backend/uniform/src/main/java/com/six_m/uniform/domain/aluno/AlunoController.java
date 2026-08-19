package com.six_m.uniform.domain.aluno;

import com.six_m.uniform.domain.aluno.dto.RequestAtualizarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.RequestCriarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.ResponseAlunoDTO;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/aluno")
public class AlunoController {

    private final AlunoService alunoService;

    @PostMapping
    public ResponseEntity<ResponseAlunoDTO> criarAluno(@Valid @RequestBody RequestCriarAlunoDTO request) {
        ResponseAlunoDTO response = alunoService.criarAluno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseAlunoDTO>> buscarTodosAlunos(Pageable pageable) {
        return ResponseEntity.ok(alunoService.buscarTodosAlunos(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseAlunoDTO> buscarAluno(@PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.buscarAluno(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ResponseAlunoDTO> atualizarAluno(@PathVariable UUID id,
                                                           @Valid @RequestBody RequestAtualizarAlunoDTO request) {
        return ResponseEntity.ok(alunoService.atualizarAluno(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponseDTO> deletarAluno(@PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.deletarAluno(id));
    }
}