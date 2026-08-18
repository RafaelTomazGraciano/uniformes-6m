package com.six_m.uniform.domain.turma;

import com.six_m.uniform.domain.turma.dto.RequestAtualizarTurmaDTO;
import com.six_m.uniform.domain.turma.dto.RequestCriarTurmaDTO;
import com.six_m.uniform.domain.turma.dto.ResponseTurmaDTO;
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
@RequestMapping("api/turma")
public class TurmaController {

    private final TurmaService turmaService;

    @PostMapping
    public ResponseEntity<ResponseTurmaDTO> criarTurma(@Valid @RequestBody RequestCriarTurmaDTO request) {
        ResponseTurmaDTO response = turmaService.criarTurma(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseTurmaDTO>> buscarTodasTurmas(Pageable pageable) {
        return ResponseEntity.ok(turmaService.buscarTodasTurmas(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseTurmaDTO> buscarTurma(@PathVariable UUID id) {
        return ResponseEntity.ok(turmaService.buscarTurma(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ResponseTurmaDTO> atualizarTurma(@PathVariable UUID id,
                                                           @Valid @RequestBody RequestAtualizarTurmaDTO request) {
        return ResponseEntity.ok(turmaService.atualizarTurma(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponseDTO> deletarTurma(@PathVariable UUID id) {
        return ResponseEntity.ok(turmaService.deletarTurma(id));
    }

}
