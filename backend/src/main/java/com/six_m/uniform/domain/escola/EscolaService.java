package com.six_m.uniform.domain.escola;

import com.six_m.uniform.domain.escola.dto.ResponseEscolaDTO;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EscolaService {

    private final EscolaRepository escolaRepository;

    @Transactional(readOnly = true)
    public Page<ResponseEscolaDTO> buscarTodasEscolas(Pageable pageable) {
        return escolaRepository.findAll(pageable)
                .map(escola -> new ResponseEscolaDTO(escola.getId(), escola.getNome(), escola.getTipo(), escola.getEndereco()));
    }

    @Transactional(readOnly = true)
    public ResponseEscolaDTO buscarEscola(UUID id) {
        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Escola não encontrada com o ID: " + id));
        return new ResponseEscolaDTO(escola.getId(), escola.getNome(), escola.getTipo(), escola.getEndereco());
    }

}
