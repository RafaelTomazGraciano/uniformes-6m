package com.six_m.uniform.domain.notaFiscal;

import com.six_m.uniform.domain.lote.LoteRepository;
import com.six_m.uniform.domain.notaFiscal.dto.RequestAtualizarNotaFiscalDTO;
import com.six_m.uniform.domain.notaFiscal.dto.RequestCriarNotaFiscalDTO;
import com.six_m.uniform.domain.notaFiscal.dto.ResponseNotaFiscalDTO;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
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
    private final LoteRepository loteRepository;

    @Transactional
    public ResponseNotaFiscalDTO criarNotaFiscal(RequestCriarNotaFiscalDTO dto) {
        if (notaFiscalRepository.existsByChaveAcesso(dto.chaveAcesso())) {
            throw new BadRequestException("Já existe uma nota fiscal com esta chave de acesso");
        }

        NotaFiscal notaFiscal = NotaFiscal.builder()
                .chaveAcesso(dto.chaveAcesso())
                .build();

        notaFiscal = notaFiscalRepository.save(notaFiscal);

        return toResponseDTO(notaFiscal);
    }

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
    public ResponseNotaFiscalDTO atualizarNotaFiscal(UUID id, RequestAtualizarNotaFiscalDTO dto) {
        NotaFiscal notaFiscal = buscarNotaFiscalOuFalhar(id);

        if (!notaFiscal.getChaveAcesso().equals(dto.chaveAcesso())
                && notaFiscalRepository.existsByChaveAcesso(dto.chaveAcesso())) {
            throw new BadRequestException("Já existe uma nota fiscal com esta chave de acesso");
        }

        notaFiscal.setChaveAcesso(dto.chaveAcesso());

        notaFiscal = notaFiscalRepository.save(notaFiscal);

        return toResponseDTO(notaFiscal);
    }

    @Transactional
    public MessageResponseDTO deletarNotaFiscal(UUID id) {
        NotaFiscal notaFiscal = buscarNotaFiscalOuFalhar(id);

        if (loteRepository.existsByNotaFiscalId(id)) {
            throw new BadRequestException("Não é possível excluir a nota fiscal: existem lotes vinculados a ela");
        }

        notaFiscalRepository.delete(notaFiscal);

        return new MessageResponseDTO("Nota fiscal deletada com sucesso");
    }

    private NotaFiscal buscarNotaFiscalOuFalhar(UUID id) {
        return notaFiscalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nota fiscal não encontrada com o ID: " + id));
    }

    private ResponseNotaFiscalDTO toResponseDTO(NotaFiscal notaFiscal) {
        return new ResponseNotaFiscalDTO(notaFiscal.getId(), notaFiscal.getChaveAcesso());
    }
}