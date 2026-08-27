package com.six_m.uniform.domain.lote;

import com.six_m.uniform.domain.lote.dto.RequestAtualizarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestCriarLoteDTO;
import com.six_m.uniform.domain.lote.dto.ResponseLoteDTO;
import com.six_m.uniform.domain.notaFiscal.NotaFiscal;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalRepository;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;
    private final NotaFiscalRepository notaFiscalRepository;

    @Transactional
    public ResponseLoteDTO criarLote(RequestCriarLoteDTO dto) {
        NotaFiscal notaFiscal = buscarNotaFiscalOuFalhar(dto.notaFiscalId());

        Lote lote = Lote.builder()
                .notaFiscal(notaFiscal)
                .fornecedor(dto.fornecedor())
                .dataEntrega(dto.dataEntrega())
                .build();

        lote = loteRepository.save(lote);

        return toResponseDTO(lote);
    }

    @Transactional(readOnly = true)
    public Page<ResponseLoteDTO> buscarTodosLotes(Pageable pageable) {
        return loteRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseLoteDTO buscarLote(UUID id) {
        return toResponseDTO(buscarLoteOuFalhar(id));
    }

    @Transactional
    public ResponseLoteDTO atualizarLote(UUID id, RequestAtualizarLoteDTO dto) {
        Lote lote = buscarLoteOuFalhar(id);
        NotaFiscal notaFiscal = buscarNotaFiscalOuFalhar(dto.notaFiscalId());

        lote.setNotaFiscal(notaFiscal);
        lote.setFornecedor(dto.fornecedor());
        lote.setDataEntrega(dto.dataEntrega());

        lote = loteRepository.save(lote);

        return toResponseDTO(lote);
    }

    private Lote buscarLoteOuFalhar(UUID id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lote não encontrado com o ID: " + id));
    }

    private NotaFiscal buscarNotaFiscalOuFalhar(UUID id) {
        return notaFiscalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nota fiscal não encontrada com o ID: " + id));
    }

    private ResponseLoteDTO toResponseDTO(Lote lote) {
        return new ResponseLoteDTO(
                lote.getId(),
                lote.getNotaFiscal().getId(),
                lote.getNotaFiscal().getChaveAcesso(),
                lote.getFornecedor(),
                lote.getDataEntrega()
        );
    }
}