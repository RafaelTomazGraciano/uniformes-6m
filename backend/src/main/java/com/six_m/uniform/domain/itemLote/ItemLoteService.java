package com.six_m.uniform.domain.itemLote;

import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemLoteService {

    private final ItemLoteRepository itemLoteRepository;

    @Transactional(readOnly = true)
    public Page<ResponseItemLoteDTO> buscarTodosItensLote(Pageable pageable) {
        return itemLoteRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseItemLoteDTO buscarItemLote(UUID id) {
        return toResponseDTO(buscarItemLoteOuFalhar(id));
    }

    private ItemLote buscarItemLoteOuFalhar(UUID id) {
        return itemLoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item de lote não encontrado com o ID: " + id));
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