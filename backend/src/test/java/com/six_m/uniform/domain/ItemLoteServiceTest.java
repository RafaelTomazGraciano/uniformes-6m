package com.six_m.uniform.domain;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteRepository;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.lote.dto.RequestItemEntradaDTO;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeService;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemLoteServiceTest {

    @Mock
    private ItemLoteRepository itemLoteRepository;

    @Mock
    private TipoUniformeService tipoUniformeService;

    @InjectMocks
    private ItemLoteService itemLoteService;

    @Test
    void deveCriarItensParaLoteComSucesso() {
        UUID tipoId = UUID.randomUUID();
        Lote lote = Lote.builder().id(UUID.randomUUID()).build();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(tipoId).tipo("Camiseta").build();
        RequestItemEntradaDTO itemDto = new RequestItemEntradaDTO(tipoId, Tamanho.M, Sexo.MASCULINO, 10);

        when(tipoUniformeService.buscarTipoUniformeEntidade(tipoId)).thenReturn(tipoUniforme);
        when(itemLoteRepository.save(any(ItemLote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<ItemLote> resultado = itemLoteService.criarItensParaLote(lote, List.of(itemDto));

        assertEquals(1, resultado.size());
        assertEquals(tipoUniforme, resultado.getFirst().getTipoUniforme());
        assertEquals(Tamanho.M, resultado.getFirst().getTamanho());
        assertEquals(10, resultado.getFirst().getQuantidade());
    }

    @Test
    void deveLancarExcecaoQuandoHaItensDuplicadosNoLote() {
        UUID tipoId = UUID.randomUUID();
        Lote lote = Lote.builder().id(UUID.randomUUID()).build();
        RequestItemEntradaDTO item1 = new RequestItemEntradaDTO(tipoId, Tamanho.M, Sexo.MASCULINO, 5);
        RequestItemEntradaDTO item2 = new RequestItemEntradaDTO(tipoId, Tamanho.M, Sexo.MASCULINO, 3);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> itemLoteService.criarItensParaLote(lote, List.of(item1, item2)));

        assertEquals("Item duplicado no lote: mesmo tipo de uniforme, tamanho e sexo informados mais de uma vez", exception.getMessage());
        verify(itemLoteRepository, never()).save(any());
    }

    @Test
    void devePermitirItensComMesmoTipoMasTamanhoOuSexoDiferentes() {
        UUID tipoId = UUID.randomUUID();
        Lote lote = Lote.builder().id(UUID.randomUUID()).build();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(tipoId).tipo("Camiseta").build();
        RequestItemEntradaDTO item1 = new RequestItemEntradaDTO(tipoId, Tamanho.M, Sexo.MASCULINO, 5);
        RequestItemEntradaDTO item2 = new RequestItemEntradaDTO(tipoId, Tamanho.G, Sexo.MASCULINO, 3);

        when(tipoUniformeService.buscarTipoUniformeEntidade(tipoId)).thenReturn(tipoUniforme);
        when(itemLoteRepository.save(any(ItemLote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<ItemLote> resultado = itemLoteService.criarItensParaLote(lote, List.of(item1, item2));

        assertEquals(2, resultado.size());
    }

    @Test
    void deveLancarExcecaoQuandoTipoUniformeNaoExisteAoCriarItensParaLote() {
        UUID tipoId = UUID.randomUUID();
        Lote lote = Lote.builder().id(UUID.randomUUID()).build();
        RequestItemEntradaDTO itemDto = new RequestItemEntradaDTO(tipoId, Tamanho.M, Sexo.MASCULINO, 10);

        when(tipoUniformeService.buscarTipoUniformeEntidade(tipoId))
                .thenThrow(new NotFoundException("Tipo de uniforme não encontrado com o ID: " + tipoId));

        assertThrows(NotFoundException.class,
                () -> itemLoteService.criarItensParaLote(lote, List.of(itemDto)));

        verify(itemLoteRepository, never()).save(any());
    }


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

    @Test
    void deveBuscarItensPorLote() {
        UUID loteId = UUID.randomUUID();
        ItemLote item = ItemLote.builder().id(UUID.randomUUID()).build();

        when(itemLoteRepository.findByLoteId(loteId)).thenReturn(List.of(item));

        List<ItemLote> resultado = itemLoteService.buscarItensPorLote(loteId);

        assertEquals(1, resultado.size());
        verify(itemLoteRepository).findByLoteId(loteId);
    }

    @Test
    void deveDeletarItensPorLote() {
        ItemLote item1 = ItemLote.builder().id(UUID.randomUUID()).build();
        ItemLote item2 = ItemLote.builder().id(UUID.randomUUID()).build();

        itemLoteService.deletarItensPorLote(List.of(item1, item2));

        verify(itemLoteRepository).deleteAll(List.of(item1, item2));
    }

    @Test
    void deveBuscarItensPorPeriodo() {
        LocalDateTime inicio = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 3, 31, 23, 59, 59);
        ItemLote item = ItemLote.builder().id(UUID.randomUUID()).build();

        when(itemLoteRepository.findByLoteDataEntregaBetween(inicio, fim)).thenReturn(List.of(item));

        List<ItemLote> resultado = itemLoteService.buscarItensPorPeriodo(inicio, fim);

        assertEquals(1, resultado.size());
        verify(itemLoteRepository).findByLoteDataEntregaBetween(inicio, fim);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaItensNoPeriodo() {
        LocalDateTime inicio = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 3, 31, 23, 59, 59);

        when(itemLoteRepository.findByLoteDataEntregaBetween(inicio, fim)).thenReturn(List.of());

        List<ItemLote> resultado = itemLoteService.buscarItensPorPeriodo(inicio, fim);

        assertTrue(resultado.isEmpty());
    }
}