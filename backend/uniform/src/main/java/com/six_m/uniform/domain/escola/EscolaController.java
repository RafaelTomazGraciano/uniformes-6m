package com.six_m.uniform.domain.escola;

import com.six_m.uniform.domain.escola.dto.ResponseEscolaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/escola")
public class EscolaController {

    private final EscolaService escolaService;

    @GetMapping
    public ResponseEntity<Page<ResponseEscolaDTO>> buscarTodasEscolas(Pageable pageable) {
        return ResponseEntity.ok(escolaService.buscarTodasEscolas(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseEscolaDTO> buscarEscola(@PathVariable UUID id) {
        return ResponseEntity.ok(escolaService.buscarEscola(id));
    }

}
