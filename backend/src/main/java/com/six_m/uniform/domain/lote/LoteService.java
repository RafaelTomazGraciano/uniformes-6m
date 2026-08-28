package com.six_m.uniform.domain.lote;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestAtualizarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestCriarLoteDTO;
import com.six_m.uniform.domain.lote.dto.ResponseLoteDTO;
import com.six_m.uniform.domain.notaFiscal.NotaFiscal;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalService;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;
    private final NotaFiscalService notaFiscalService;
    private final ItemLoteService itemLoteService;
    private final UniformeService uniformeService;

    @Transactional
    public ResponseLoteDTO criarLote(RequestCriarLoteDTO dto) {
        NotaFiscal notaFiscal = notaFiscalService.criarParaLote(dto.chaveAcesso());

        Lote lote = Lote.builder()
                .notaFiscal(notaFiscal)
                .fornecedor(dto.fornecedor())
                .dataEntrega(dto.dataEntrega())
                .build();

        lote = loteRepository.save(lote);

        List<ItemLote> itens = itemLoteService.criarItensParaLote(lote, dto.itens());

        for (ItemLote item : itens) {
            uniformeService.darEntrada(item.getTipoUniforme().getId(), item.getTamanho(), item.getSexo(), item.getQuantidade());
        }

        return toResponseDTO(lote, itens);
    }

    @Transactional(readOnly = true)
    public Page<ResponseLoteDTO> buscarTodosLotes(Pageable pageable) {
        return loteRepository.findAll(pageable)
                .map(lote -> toResponseDTO(lote, itemLoteService.buscarItensPorLote(lote.getId())));
    }

    @Transactional(readOnly = true)
    public ResponseLoteDTO buscarLote(UUID id) {
        Lote lote = buscarLoteOuFalhar(id);
        List<ItemLote> itens = itemLoteService.buscarItensPorLote(id);
        return toResponseDTO(lote, itens);
    }

    @Transactional
    public ResponseLoteDTO atualizarLote(UUID id, RequestAtualizarLoteDTO dto) {
        Lote lote = buscarLoteOuFalhar(id);
        NotaFiscal notaFiscal = notaFiscalService.atualizarParaLote(lote.getNotaFiscal(), dto.chaveAcesso());

        List<ItemLote> itensAntigos = itemLoteService.buscarItensPorLote(id);
        for (ItemLote itemAntigo : itensAntigos) {
            uniformeService.estornarEntrada(itemAntigo.getTipoUniforme().getId(), itemAntigo.getTamanho(), itemAntigo.getSexo(), itemAntigo.getQuantidade());
        }
        itemLoteService.deletarItensPorLote(itensAntigos);

        List<ItemLote> itensNovos = itemLoteService.criarItensParaLote(lote, dto.itens());
        for (ItemLote itemNovo : itensNovos) {
            uniformeService.darEntrada(itemNovo.getTipoUniforme().getId(), itemNovo.getTamanho(), itemNovo.getSexo(), itemNovo.getQuantidade());
        }

        lote.setNotaFiscal(notaFiscal);
        lote.setFornecedor(dto.fornecedor());
        lote.setDataEntrega(dto.dataEntrega());
        lote = loteRepository.save(lote);

        return toResponseDTO(lote, itensNovos);
    }


    private Lote buscarLoteOuFalhar(UUID id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lote não encontrado com o ID: " + id));
    }

    private ResponseLoteDTO toResponseDTO(Lote lote, List<ItemLote> itens) {
        List<ResponseItemLoteDTO> itensDto = itens.stream()
                .map(item -> new ResponseItemLoteDTO(
                        item.getId(),
                        item.getTipoUniforme().getId(),
                        item.getTipoUniforme().getTipo(),
                        lote.getId(),
                        lote.getFornecedor(),
                        item.getTamanho(),
                        item.getQuantidade(),
                        item.getSexo()
                ))
                .toList();

        return new ResponseLoteDTO(
                lote.getId(),
                lote.getNotaFiscal().getId(),
                lote.getNotaFiscal().getChaveAcesso(),
                lote.getFornecedor(),
                lote.getDataEntrega(),
                itensDto
        );
    }
}