package com.six_m.uniform.domain.tipoUniforme;

import com.six_m.uniform.domain.tipoUniforme.dto.RequestAtualizarTipoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.dto.RequestCriarTipoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.dto.ResponseTipoUniformeDTO;
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
@RequestMapping("api/tipo-uniforme")
public class TipoUniformeController {

    private final TipoUniformeService tipoUniformeService;

    @PostMapping
    public ResponseEntity<ResponseTipoUniformeDTO> criarTipoUniforme(@Valid @RequestBody RequestCriarTipoUniformeDTO request) {
        ResponseTipoUniformeDTO response = tipoUniformeService.criarTipoUniforme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseTipoUniformeDTO>> buscarTodosTiposUniforme(Pageable pageable) {
        return ResponseEntity.ok(tipoUniformeService.buscarTodosTiposUniforme(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseTipoUniformeDTO> buscarTipoUniforme(@PathVariable UUID id) {
        return ResponseEntity.ok(tipoUniformeService.buscarTipoUniforme(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ResponseTipoUniformeDTO> atualizarTipoUniforme(@PathVariable UUID id,
                                                                         @Valid @RequestBody RequestAtualizarTipoUniformeDTO request) {
        return ResponseEntity.ok(tipoUniformeService.atualizarTipoUniforme(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponseDTO> deletarTipoUniforme(@PathVariable UUID id) {
        return ResponseEntity.ok(tipoUniformeService.deletarTipoUniforme(id));
    }
}