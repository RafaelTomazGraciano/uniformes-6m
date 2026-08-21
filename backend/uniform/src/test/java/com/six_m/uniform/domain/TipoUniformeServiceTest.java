package com.six_m.uniform.domain;

import com.six_m.uniform.domain.itemLote.ItemLoteRepository;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeRepository;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeService;
import com.six_m.uniform.domain.tipoUniforme.dto.RequestAtualizarTipoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.dto.RequestCriarTipoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.dto.ResponseTipoUniformeDTO;
import com.six_m.uniform.domain.uniforme.UniformeRepository;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
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
public class TipoUniformeServiceTest {

    @Mock
    private TipoUniformeRepository tipoUniformeRepository;

    @Mock
    private UniformeRepository uniformeRepository;

    @Mock
    private ItemLoteRepository itemLoteRepository;

    @InjectMocks
    private TipoUniformeService tipoUniformeService;

    @Test
    void deveCriarTipoUniformeComSucesso() {
        RequestCriarTipoUniformeDTO dto = new RequestCriarTipoUniformeDTO("Camiseta");

        UUID idGerado = UUID.randomUUID();
        when(tipoUniformeRepository.save(any(TipoUniforme.class))).thenAnswer(invocation -> {
            TipoUniforme salvo = invocation.getArgument(0);
            salvo.setId(idGerado);
            return salvo;
        });

        ResponseTipoUniformeDTO response = tipoUniformeService.criarTipoUniforme(dto);

        assertEquals(idGerado, response.id());
        assertEquals("Camiseta", response.tipo());
    }

    @Test
    void deveBuscarTodosTiposUniformePaginado() {
        TipoUniforme tipo1 = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        TipoUniforme tipo2 = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Calça").build();

        Pageable pageable = PageRequest.of(0, 10);
        when(tipoUniformeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(tipo1, tipo2), pageable, 2));

        var resultado = tipoUniformeService.buscarTodosTiposUniforme(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals("Camiseta", resultado.getContent().get(0).tipo());
        assertEquals("Calça", resultado.getContent().get(1).tipo());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaTiposUniforme() {
        Pageable pageable = PageRequest.of(0, 10);
        when(tipoUniformeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = tipoUniformeService.buscarTodosTiposUniforme(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarTipoUniformePorId() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(id).tipo("Camiseta").build();

        when(tipoUniformeRepository.findById(id)).thenReturn(Optional.of(tipoUniforme));

        ResponseTipoUniformeDTO response = tipoUniformeService.buscarTipoUniforme(id);

        assertEquals(id, response.id());
        assertEquals("Camiseta", response.tipo());
    }

    @Test
    void deveLancarExcecaoQuandoTipoUniformeNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(tipoUniformeRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tipoUniformeService.buscarTipoUniforme(id));

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void deveAtualizarTipoUniformeComSucesso() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipoExistente = TipoUniforme.builder().id(id).tipo("Camiseta").build();
        RequestAtualizarTipoUniformeDTO dto = new RequestAtualizarTipoUniformeDTO("Camiseta Polo");

        when(tipoUniformeRepository.findById(id)).thenReturn(Optional.of(tipoExistente));
        when(tipoUniformeRepository.save(any(TipoUniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseTipoUniformeDTO response = tipoUniformeService.atualizarTipoUniforme(id, dto);

        assertEquals("Camiseta Polo", response.tipo());
    }

    @Test
    void deveLancarExcecaoQuandoTipoUniformeNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        RequestAtualizarTipoUniformeDTO dto = new RequestAtualizarTipoUniformeDTO("Camiseta Polo");

        when(tipoUniformeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tipoUniformeService.atualizarTipoUniforme(id, dto));
        verify(tipoUniformeRepository, never()).save(any());
    }

    @Test
    void deveDeletarTipoUniformeComSucessoQuandoNaoHaVinculos() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(id).tipo("Camiseta").build();

        when(tipoUniformeRepository.findById(id)).thenReturn(Optional.of(tipoUniforme));
        when(uniformeRepository.existsByTipoUniformeId(id)).thenReturn(false);
        when(itemLoteRepository.existsByTipoUniformeId(id)).thenReturn(false);

        MessageResponseDTO resultado = tipoUniformeService.deletarTipoUniforme(id);

        assertEquals("Tipo de uniforme deletado com sucesso", resultado.message());
        verify(tipoUniformeRepository).delete(tipoUniforme);
    }

    @Test
    void deveLancarExcecaoAoDeletarTipoUniformeComUniformeVinculado() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(id).tipo("Camiseta").build();

        when(tipoUniformeRepository.findById(id)).thenReturn(Optional.of(tipoUniforme));
        when(uniformeRepository.existsByTipoUniformeId(id)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> tipoUniformeService.deletarTipoUniforme(id));

        assertEquals("Não é possível excluir o tipo de uniforme: existem uniformes vinculados a ele", exception.getMessage());
        verify(itemLoteRepository, never()).existsByTipoUniformeId(any());
        verify(tipoUniformeRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoAoDeletarTipoUniformeComItemLoteVinculado() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(id).tipo("Camiseta").build();

        when(tipoUniformeRepository.findById(id)).thenReturn(Optional.of(tipoUniforme));
        when(uniformeRepository.existsByTipoUniformeId(id)).thenReturn(false);
        when(itemLoteRepository.existsByTipoUniformeId(id)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> tipoUniformeService.deletarTipoUniforme(id));

        assertEquals("Não é possível excluir o tipo de uniforme: existem itens em lote vinculados a ele", exception.getMessage());
        verify(tipoUniformeRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoQuandoTipoUniformeNaoExisteAoDeletar() {
        UUID id = UUID.randomUUID();
        when(tipoUniformeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tipoUniformeService.deletarTipoUniforme(id));
        verify(uniformeRepository, never()).existsByTipoUniformeId(any());
        verify(itemLoteRepository, never()).existsByTipoUniformeId(any());
    }
}