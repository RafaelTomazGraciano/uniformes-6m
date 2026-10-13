package com.six_m.uniform.domain.itemLote;

import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
import com.six_m.uniform.domain.lote.Lote;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        Map<ChaveItemLote, Integer> quantidadePorItem = consolidarItens(itensDto);

        List<ItemLote> itens = new ArrayList<>();
        for (Map.Entry<ChaveItemLote, Integer> entrada : quantidadePorItem.entrySet()) {
            ChaveItemLote chave = entrada.getKey();
            TipoUniforme tipoUniforme = tipoUniformeService.buscarTipoUniformeEntidade(chave.tipoUniformeId());

            ItemLote itemLote = ItemLote.builder()
                    .tipoUniforme(tipoUniforme)
                    .lote(lote)
                    .tamanho(chave.tamanho())
                    .sexo(chave.sexo())
                    .quantidade(entrada.getValue())
                    .build();

            itens.add(itemLoteRepository.save(itemLote));
        }

        return itens;
    }

    @Transactional(readOnly = true)
    public List<ItemLote> buscarItensPorLote(UUID loteId) {
        return itemLoteRepository.findByLoteId(loteId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<ItemLote>> buscarItensPorLotes(Collection<UUID> loteIds) {
        return itemLoteRepository.findByLoteIdIn(loteIds).stream()
                .collect(Collectors.groupingBy(item -> item.getLote().getId()));
    }

    @Transactional
    public void deletarItensPorLote(List<ItemLote> itens) {
        itemLoteRepository.deleteAll(itens);
    }

    @Transactional(readOnly = true)
    public List<ItemLote> buscarItensPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return itemLoteRepository.findByLoteDataEntregaBetween(inicio, fim);
    }

    private ItemLote buscarItemLoteOuFalhar(UUID id) {
        return itemLoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item de lote não encontrado com o ID: " + id));
    }

    private Map<ChaveItemLote, Integer> consolidarItens(List<RequestItemEntradaDTO> itensDto) {
        Map<ChaveItemLote, Integer> consolidado = new LinkedHashMap<>();
        for (RequestItemEntradaDTO item : itensDto) {
            ChaveItemLote chave = new ChaveItemLote(item.tipoUniformeId(), item.tamanho(), item.sexo());
            consolidado.merge(chave, item.quantidade(), this::somarQuantidades);
        }
        return consolidado;
    }

    private Integer somarQuantidades(Integer a, Integer b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException e) {
            throw new BadRequestException("Quantidade total do item excede o limite permitido");
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