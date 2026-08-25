package com.six_m.uniform.domain;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteRepository;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.itemLote.dto.RequestAtualizarItemLoteDTO;
import com.six_m.uniform.domain.itemLote.dto.RequestCriarItemLoteDTO;
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

    @Mock
    private TipoUniformeRepository tipoUniformeRepository;

    @Mock
    private LoteRepository loteRepository;

    @InjectMocks
    private ItemLoteService itemLoteService;

    @Test
    void deveCriarItemLoteComSucesso() {
        UUID tipoId = UUID.randomUUID();
        UUID loteId = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(tipoId).tipo("Camiseta").build();
        Lote lote = Lote.builder().id(loteId).fornecedor("Fornecedor A").build();
        RequestCriarItemLoteDTO dto = new RequestCriarItemLoteDTO(tipoId, loteId, Tamanho.M, 15, Sexo.MASCULINO);

        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.of(tipoUniforme));
        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));

        UUID idGerado = UUID.randomUUID();
        when(itemLoteRepository.save(any(ItemLote.class))).thenAnswer(invocation -> {
            ItemLote salvo = invocation.getArgument(0);
            salvo.setId(idGerado);
            return salvo;
        });

        ResponseItemLoteDTO response = itemLoteService.criarItemLote(dto);

        assertEquals(idGerado, response.id());
        assertEquals(tipoId, response.tipoUniformeId());
        assertEquals("Camiseta", response.tipoUniformeNome());
        assertEquals(loteId, response.loteId());
        assertEquals("Fornecedor A", response.loteFornecedor());
        assertEquals(Tamanho.M, response.tamanho());
        assertEquals(15, response.quantidade());
        assertEquals(Sexo.MASCULINO, response.sexo());
    }

    @Test
    void deveLancarExcecaoQuandoTipoUniformeNaoExisteAoCriar() {
        UUID tipoId = UUID.randomUUID();
        RequestCriarItemLoteDTO dto = new RequestCriarItemLoteDTO(tipoId, UUID.randomUUID(), Tamanho.M, 15, Sexo.MASCULINO);

        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemLoteService.criarItemLote(dto));

        assertTrue(exception.getMessage().contains(tipoId.toString()));
        verify(loteRepository, never()).findById(any());
        verify(itemLoteRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoLoteNaoExisteAoCriar() {
        UUID tipoId = UUID.randomUUID();
        UUID loteId = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(tipoId).tipo("Camiseta").build();
        RequestCriarItemLoteDTO dto = new RequestCriarItemLoteDTO(tipoId, loteId, Tamanho.M, 15, Sexo.MASCULINO);

        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.of(tipoUniforme));
        when(loteRepository.findById(loteId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemLoteService.criarItemLote(dto));

        assertTrue(exception.getMessage().contains(loteId.toString()));
        verify(itemLoteRepository, never()).save(any());
    }

    @Test
    void deveSalvarItemLoteComOsCamposCorretosAoCriar() {
        UUID tipoId = UUID.randomUUID();
        UUID loteId = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(tipoId).tipo("Calça").build();
        Lote lote = Lote.builder().id(loteId).fornecedor("Fornecedor B").build();
        RequestCriarItemLoteDTO dto = new RequestCriarItemLoteDTO(tipoId, loteId, Tamanho.GG, 8, Sexo.FEMININO);

        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.of(tipoUniforme));
        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(itemLoteRepository.save(any(ItemLote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        itemLoteService.criarItemLote(dto);

        ArgumentCaptor<ItemLote> captor = ArgumentCaptor.forClass(ItemLote.class);
        verify(itemLoteRepository).save(captor.capture());

        assertEquals(tipoUniforme, captor.getValue().getTipoUniforme());
        assertEquals(lote, captor.getValue().getLote());
        assertEquals(Tamanho.GG, captor.getValue().getTamanho());
        assertEquals(8, captor.getValue().getQuantidade());
        assertEquals(Sexo.FEMININO, captor.getValue().getSexo());
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
    void deveAtualizarItemLoteComSucesso() {
        UUID id = UUID.randomUUID();
        UUID novoTipoId = UUID.randomUUID();
        UUID novoLoteId = UUID.randomUUID();

        TipoUniforme tipoAntigo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Lote loteAntigo = Lote.builder().id(UUID.randomUUID()).fornecedor("Fornecedor A").build();
        ItemLote itemExistente = ItemLote.builder().id(id).tipoUniforme(tipoAntigo).lote(loteAntigo).tamanho(Tamanho.P).quantidade(5).sexo(Sexo.MASCULINO).build();

        TipoUniforme tipoNovo = TipoUniforme.builder().id(novoTipoId).tipo("Calça").build();
        Lote loteNovo = Lote.builder().id(novoLoteId).fornecedor("Fornecedor B").build();
        RequestAtualizarItemLoteDTO dto = new RequestAtualizarItemLoteDTO(novoTipoId, novoLoteId, Tamanho.GG, 20, Sexo.FEMININO);

        when(itemLoteRepository.findById(id)).thenReturn(Optional.of(itemExistente));
        when(tipoUniformeRepository.findById(novoTipoId)).thenReturn(Optional.of(tipoNovo));
        when(loteRepository.findById(novoLoteId)).thenReturn(Optional.of(loteNovo));
        when(itemLoteRepository.save(any(ItemLote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseItemLoteDTO response = itemLoteService.atualizarItemLote(id, dto);

        assertEquals(novoTipoId, response.tipoUniformeId());
        assertEquals("Calça", response.tipoUniformeNome());
        assertEquals(novoLoteId, response.loteId());
        assertEquals("Fornecedor B", response.loteFornecedor());
        assertEquals(Tamanho.GG, response.tamanho());
        assertEquals(20, response.quantidade());
        assertEquals(Sexo.FEMININO, response.sexo());
    }

    @Test
    void deveLancarExcecaoQuandoItemLoteNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        RequestAtualizarItemLoteDTO dto = new RequestAtualizarItemLoteDTO(UUID.randomUUID(), UUID.randomUUID(), Tamanho.M, 10, Sexo.MASCULINO);

        when(itemLoteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemLoteService.atualizarItemLote(id, dto));
        verify(tipoUniformeRepository, never()).findById(any());
        verify(loteRepository, never()).findById(any());
        verify(itemLoteRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNovoTipoUniformeNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();

        TipoUniforme tipoAntigo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Lote loteAntigo = Lote.builder().id(UUID.randomUUID()).fornecedor("Fornecedor A").build();
        ItemLote itemExistente = ItemLote.builder().id(id).tipoUniforme(tipoAntigo).lote(loteAntigo).tamanho(Tamanho.M).quantidade(10).sexo(Sexo.MASCULINO).build();
        RequestAtualizarItemLoteDTO dto = new RequestAtualizarItemLoteDTO(tipoId, UUID.randomUUID(), Tamanho.M, 10, Sexo.MASCULINO);

        when(itemLoteRepository.findById(id)).thenReturn(Optional.of(itemExistente));
        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemLoteService.atualizarItemLote(id, dto));

        assertTrue(exception.getMessage().contains(tipoId.toString()));
        verify(loteRepository, never()).findById(any());
        verify(itemLoteRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNovoLoteNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        UUID loteId = UUID.randomUUID();

        TipoUniforme tipoAntigo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Lote loteAntigo = Lote.builder().id(UUID.randomUUID()).fornecedor("Fornecedor A").build();
        ItemLote itemExistente = ItemLote.builder().id(id).tipoUniforme(tipoAntigo).lote(loteAntigo).tamanho(Tamanho.M).quantidade(10).sexo(Sexo.MASCULINO).build();
        TipoUniforme tipoNovo = TipoUniforme.builder().id(tipoId).tipo("Calça").build();
        RequestAtualizarItemLoteDTO dto = new RequestAtualizarItemLoteDTO(tipoId, loteId, Tamanho.M, 10, Sexo.MASCULINO);

        when(itemLoteRepository.findById(id)).thenReturn(Optional.of(itemExistente));
        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.of(tipoNovo));
        when(loteRepository.findById(loteId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemLoteService.atualizarItemLote(id, dto));

        assertTrue(exception.getMessage().contains(loteId.toString()));
        verify(itemLoteRepository, never()).save(any());
    }

    @Test
    void deveDeletarItemLoteComSucesso() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Lote lote = Lote.builder().id(UUID.randomUUID()).fornecedor("Fornecedor A").build();
        ItemLote itemLote = ItemLote.builder().id(id).tipoUniforme(tipo).lote(lote).tamanho(Tamanho.M).quantidade(10).sexo(Sexo.MASCULINO).build();

        when(itemLoteRepository.findById(id)).thenReturn(Optional.of(itemLote));

        MessageResponseDTO resultado = itemLoteService.deletarItemLote(id);

        assertEquals("Item de lote deletado com sucesso", resultado.message());
        verify(itemLoteRepository).delete(itemLote);
    }

    @Test
    void deveLancarExcecaoQuandoItemLoteNaoExisteAoDeletar() {
        UUID id = UUID.randomUUID();
        when(itemLoteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemLoteService.deletarItemLote(id));
        verify(itemLoteRepository, never()).delete(any());
    }
}