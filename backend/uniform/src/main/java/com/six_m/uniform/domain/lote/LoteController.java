package com.six_m.uniform.domain.lote;

import com.six_m.uniform.domain.lote.dto.RequestAtualizarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestCriarLoteDTO;
import com.six_m.uniform.domain.lote.dto.ResponseLoteDTO;
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
@RequestMapping("api/lote")
public class LoteController {

    private final LoteService loteService;

    @PostMapping
    public ResponseEntity<ResponseLoteDTO> criarLote(@Valid @RequestBody RequestCriarLoteDTO request) {
        ResponseLoteDTO response = loteService.criarLote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseLoteDTO>> buscarTodosLotes(Pageable pageable) {
        return ResponseEntity.ok(loteService.buscarTodosLotes(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseLoteDTO> buscarLote(@PathVariable UUID id) {
        return ResponseEntity.ok(loteService.buscarLote(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ResponseLoteDTO> atualizarLote(@PathVariable UUID id,
                                                         @Valid @RequestBody RequestAtualizarLoteDTO request) {
        return ResponseEntity.ok(loteService.atualizarLote(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponseDTO> deletarLote(@PathVariable UUID id) {
        return ResponseEntity.ok(loteService.deletarLote(id));
    }
}