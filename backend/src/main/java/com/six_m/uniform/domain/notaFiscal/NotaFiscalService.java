package com.six_m.uniform.domain.notaFiscal;

import com.six_m.uniform.domain.notaFiscal.dto.ResponseNotaFiscalDTO;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotaFiscalService {

    private final NotaFiscalRepository notaFiscalRepository;

    @Transactional(readOnly = true)
    public Page<ResponseNotaFiscalDTO> buscarTodasNotasFiscais(Pageable pageable) {
        return notaFiscalRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseNotaFiscalDTO buscarNotaFiscal(UUID id) {
        return toResponseDTO(buscarNotaFiscalOuFalhar(id));
    }

    private NotaFiscal buscarNotaFiscalOuFalhar(UUID id) {
        return notaFiscalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nota fiscal não encontrada com o ID: " + id));
    }

    private ResponseNotaFiscalDTO toResponseDTO(NotaFiscal notaFiscal) {
        return new ResponseNotaFiscalDTO(notaFiscal.getId(), notaFiscal.getChaveAcesso());
    }
}