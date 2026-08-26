package com.six_m.uniform.domain.itemLote;

import com.six_m.uniform.domain.itemLote.dto.RequestAtualizarItemLoteDTO;
import com.six_m.uniform.domain.itemLote.dto.RequestCriarItemLoteDTO;
import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.lote.LoteRepository;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeRepository;
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
public class ItemLoteService {

    private final ItemLoteRepository itemLoteRepository;
    private final TipoUniformeRepository tipoUniformeRepository;
    private final LoteRepository loteRepository;

    @Transactional
    public ResponseItemLoteDTO criarItemLote(RequestCriarItemLoteDTO dto) {
        TipoUniforme tipoUniforme = buscarTipoUniformeOuFalhar(dto.tipoUniformeId());
        Lote lote = buscarLoteOuFalhar(dto.loteId());

        ItemLote itemLote = ItemLote.builder()
                .tipoUniforme(tipoUniforme)
                .lote(lote)
                .tamanho(dto.tamanho())
                .quantidade(dto.quantidade())
                .sexo(dto.sexo())
                .build();

        itemLote = itemLoteRepository.save(itemLote);

        return toResponseDTO(itemLote);
    }

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
    public ResponseItemLoteDTO atualizarItemLote(UUID id, RequestAtualizarItemLoteDTO dto) {
        ItemLote itemLote = buscarItemLoteOuFalhar(id);
        TipoUniforme tipoUniforme = buscarTipoUniformeOuFalhar(dto.tipoUniformeId());
        Lote lote = buscarLoteOuFalhar(dto.loteId());

        itemLote.setTipoUniforme(tipoUniforme);
        itemLote.setLote(lote);
        itemLote.setTamanho(dto.tamanho());
        itemLote.setQuantidade(dto.quantidade());
        itemLote.setSexo(dto.sexo());

        itemLote = itemLoteRepository.save(itemLote);

        return toResponseDTO(itemLote);
    }

    @Transactional
    public MessageResponseDTO deletarItemLote(UUID id) {
        ItemLote itemLote = buscarItemLoteOuFalhar(id);
        itemLoteRepository.delete(itemLote);
        return new MessageResponseDTO("Item de lote deletado com sucesso");
    }

    private ItemLote buscarItemLoteOuFalhar(UUID id) {
        return itemLoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item de lote não encontrado com o ID: " + id));
    }

    private TipoUniforme buscarTipoUniformeOuFalhar(UUID id) {
        return tipoUniformeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de uniforme não encontrado com o ID: " + id));
    }

    private Lote buscarLoteOuFalhar(UUID id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lote não encontrado com o ID: " + id));
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