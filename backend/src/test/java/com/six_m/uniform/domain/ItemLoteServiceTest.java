package com.six_m.uniform.domain;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteRepository;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.lote.LoteRepository;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeRepository;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemLoteServiceTest {

    @Mock
    private ItemLoteRepository itemLoteRepository;

    @InjectMocks
    private ItemLoteService itemLoteService;

    @Test
    void deveBuscarTodosItensLotePaginado() {
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Lote lote = Lote.builder().id(UUID.randomUUID()).fornecedor("Fornecedor A").build();
        ItemLote item1 = ItemLote.builder().id(UUID.randomUUID()).tipoUniforme(tipo).lote(lote).tamanho(Tamanho.P).quantidade(3).sexo(Sexo.MASCULINO).build();
        ItemLote item2 = ItemLote.builder().id(UUID.randomUUID()).tipoUniforme(tipo).lote(lote).tamanho(Tamanho.G).quantidade(7).sexo(Sexo.FEMININO).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(itemLoteRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(item1, item2), pageable, 2));

        var resultado = itemLoteService.buscarTodosItensLote(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals(Tamanho.P, resultado.getContent().get(0).tamanho());
        assertEquals(Tamanho.G, resultado.getContent().get(1).tamanho());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaItensLote() {
        Pageable pageable = PageRequest.of(0, 10);
        when(itemLoteRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = itemLoteService.buscarTodosItensLote(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarItemLotePorId() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Lote lote = Lote.builder().id(UUID.randomUUID()).fornecedor("Fornecedor A").build();
        ItemLote itemLote = ItemLote.builder().id(id).tipoUniforme(tipo).lote(lote).tamanho(Tamanho.M).quantidade(10).sexo(Sexo.MASCULINO).build();

        when(itemLoteRepository.findById(id)).thenReturn(Optional.of(itemLote));

        ResponseItemLoteDTO response = itemLoteService.buscarItemLote(id);

        assertEquals(id, response.id());
        assertEquals(Tamanho.M, response.tamanho());
    }

    @Test
    void deveLancarExcecaoQuandoItemLoteNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(itemLoteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemLoteService.buscarItemLote(id));
    }
}