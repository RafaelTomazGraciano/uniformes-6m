package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.domain.uniforme.dto.RequestAtualizarUniformeDTO;
import com.six_m.uniform.domain.uniforme.dto.RequestCriarUniformeDTO;
import com.six_m.uniform.domain.uniforme.dto.ResponseUniformeDTO;
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
@RequestMapping("api/uniforme")
public class UniformeController {

    private final UniformeService uniformeService;

    @PostMapping
    public ResponseEntity<ResponseUniformeDTO> criarUniforme(@Valid @RequestBody RequestCriarUniformeDTO request) {
        ResponseUniformeDTO response = uniformeService.criarUniforme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseUniformeDTO>> buscarTodosUniformes(Pageable pageable) {
        return ResponseEntity.ok(uniformeService.buscarTodosUniformes(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseUniformeDTO> buscarUniforme(@PathVariable UUID id) {
        return ResponseEntity.ok(uniformeService.buscarUniforme(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ResponseUniformeDTO> atualizarUniforme(@PathVariable UUID id,
                                                                 @Valid @RequestBody RequestAtualizarUniformeDTO request) {
        return ResponseEntity.ok(uniformeService.atualizarUniforme(id, request));
    }

    @PatchMapping("{id}/devolver")
    public ResponseEntity<ResponseUniformeDTO> devolverUniforme(@PathVariable UUID id) {
        return ResponseEntity.ok(uniformeService.devolverUniforme(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponseDTO> deletarUniforme(@PathVariable UUID id) {
        return ResponseEntity.ok(uniformeService.deletarUniforme(id));
    }
}