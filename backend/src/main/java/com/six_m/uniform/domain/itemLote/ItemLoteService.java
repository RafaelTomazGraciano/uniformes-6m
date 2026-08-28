package com.six_m.uniform.domain.itemLote;

import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.lote.dto.RequestCriarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestItemEntradaDTO;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeService;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemLoteService {

    private final ItemLoteRepository itemLoteRepository;
    private final TipoUniformeService tipoUniformeService;

    @Transactional(readOnly = true)
    public Page<ResponseItemLoteDTO> buscarTodosItensLote(Pageable pageable) {
        return itemLoteRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseItemLoteDTO buscarItemLote(UUID id) {
        return toResponseDTO(buscarItemLoteOuFalhar(id));
    }

    @Transactional
    public List<ItemLote> criarItensParaLote(Lote lote, List<RequestItemEntradaDTO> itensDto) {
        validarItensSemDuplicidade(itensDto);

        List<ItemLote> itens = new ArrayList<>();
        for (RequestItemEntradaDTO itemDto : itensDto) {
            TipoUniforme tipoUniforme = tipoUniformeService.buscarTipoUniformeEntidade(itemDto.tipoUniformeId());

            ItemLote itemLote = ItemLote.builder()
                    .tipoUniforme(tipoUniforme)
                    .lote(lote)
                    .tamanho(itemDto.tamanho())
                    .sexo(itemDto.sexo())
                    .quantidade(itemDto.quantidade())
                    .build();

            itens.add(itemLoteRepository.save(itemLote));
        }

        return itens;
    }

    @Transactional(readOnly = true)
    public List<ItemLote> buscarItensPorLote(UUID loteId) {
        return itemLoteRepository.findByLoteId(loteId);
    }

    @Transactional
    public void deletarItensPorLote(List<ItemLote> itens) {
        itemLoteRepository.deleteAll(itens);
    }

    private ItemLote buscarItemLoteOuFalhar(UUID id) {
        return itemLoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item de lote não encontrado com o ID: " + id));
    }

    private void validarItensSemDuplicidade(List<RequestItemEntradaDTO> itensDto) {
        Set<String> combinacoesVistas = new HashSet<>();
        for (RequestItemEntradaDTO item : itensDto) {
            String chave = item.tipoUniformeId() + "|" + item.tamanho() + "|" + item.sexo();
            if (!combinacoesVistas.add(chave)) {
                throw new BadRequestException("Item duplicado no lote: mesmo tipo de uniforme, tamanho e sexo informados mais de uma vez");
            }
        }
    }

    private ResponseItemLoteDTO toResponseDTO(ItemLote itemLote) {
        return new ResponseItemLoteDTO(
                itemLote.getId(),
                itemLote.getTipoUniforme().getId(),
                itemLote.getTipoUniforme().getTipo(),
                itemLote.getLote().getId(),
                itemLote.getLote().getFornecedor(),
                itemLote.getTamanho(),
                itemLote.getQuantidade(),
                itemLote.getSexo()
        );
    }
}