package com.six_m.uniform.domain.notaFiscal;

import com.six_m.uniform.domain.notaFiscal.dto.ResponseNotaFiscalDTO;
import com.six_m.uniform.exception.BadRequestException;
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

    @Transactional
    public NotaFiscal criarParaLote(String chaveAcesso) {
        if (notaFiscalRepository.existsByChaveAcesso(chaveAcesso)) {
            throw new BadRequestException("Já existe uma nota fiscal com esta chave de acesso");
        }

        NotaFiscal notaFiscal = NotaFiscal.builder()
                .chaveAcesso(chaveAcesso)
                .build();

        return notaFiscalRepository.save(notaFiscal);
    }

    @Transactional
    public NotaFiscal atualizarParaLote(NotaFiscal notaFiscal, String novaChaveAcesso) {
        if (!notaFiscal.getChaveAcesso().equals(novaChaveAcesso)
                && notaFiscalRepository.existsByChaveAcessoAndIdNot(novaChaveAcesso, notaFiscal.getId())) {
            throw new BadRequestException("Já existe uma nota fiscal com esta chave de acesso");
        }

        notaFiscal.setChaveAcesso(novaChaveAcesso);

        return notaFiscalRepository.save(notaFiscal);
    }

    private NotaFiscal buscarNotaFiscalOuFalhar(UUID id) {
        return notaFiscalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nota fiscal não encontrada com o ID: " + id));
    }

    private ResponseNotaFiscalDTO toResponseDTO(NotaFiscal notaFiscal) {
        return new ResponseNotaFiscalDTO(notaFiscal.getId(), notaFiscal.getChaveAcesso());
    }
}