package com.six_m.uniform.domain.notaFiscal;

import com.six_m.uniform.domain.notaFiscal.dto.RequestAtualizarNotaFiscalDTO;
import com.six_m.uniform.domain.notaFiscal.dto.RequestCriarNotaFiscalDTO;
import com.six_m.uniform.domain.notaFiscal.dto.ResponseNotaFiscalDTO;
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
@RequestMapping("api/nota-fiscal")
public class NotaFiscalController {

    private final NotaFiscalService notaFiscalService;

    @PostMapping
    public ResponseEntity<ResponseNotaFiscalDTO> criarNotaFiscal(@Valid @RequestBody RequestCriarNotaFiscalDTO request) {
        ResponseNotaFiscalDTO response = notaFiscalService.criarNotaFiscal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseNotaFiscalDTO>> buscarTodasNotasFiscais(Pageable pageable) {
        return ResponseEntity.ok(notaFiscalService.buscarTodasNotasFiscais(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseNotaFiscalDTO> buscarNotaFiscal(@PathVariable UUID id) {
        return ResponseEntity.ok(notaFiscalService.buscarNotaFiscal(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ResponseNotaFiscalDTO> atualizarNotaFiscal(@PathVariable UUID id,
                                                                     @Valid @RequestBody RequestAtualizarNotaFiscalDTO request) {
        return ResponseEntity.ok(notaFiscalService.atualizarNotaFiscal(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponseDTO> deletarNotaFiscal(@PathVariable UUID id) {
        return ResponseEntity.ok(notaFiscalService.deletarNotaFiscal(id));
    }
}