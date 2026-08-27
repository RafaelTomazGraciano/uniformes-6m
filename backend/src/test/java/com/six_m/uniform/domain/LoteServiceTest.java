package com.six_m.uniform.domain;

import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.lote.LoteRepository;
import com.six_m.uniform.domain.lote.LoteService;
import com.six_m.uniform.domain.lote.dto.RequestAtualizarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestCriarLoteDTO;
import com.six_m.uniform.domain.lote.dto.ResponseLoteDTO;
import com.six_m.uniform.domain.notaFiscal.NotaFiscal;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalRepository;
import com.six_m.uniform.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
public class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private NotaFiscalRepository notaFiscalRepository;

    @InjectMocks
    private LoteService loteService;

    @Test
    void deveCriarLoteComSucesso() {
        UUID notaFiscalId = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(notaFiscalId).chaveAcesso("chave-1").build();
        LocalDateTime dataEntrega = LocalDateTime.of(2025, 6, 17, 14, 30);
        RequestCriarLoteDTO dto = new RequestCriarLoteDTO(notaFiscalId, "Fornecedor A", dataEntrega);

        when(notaFiscalRepository.findById(notaFiscalId)).thenReturn(Optional.of(notaFiscal));

        UUID idGerado = UUID.randomUUID();
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> {
            Lote salvo = invocation.getArgument(0);
            salvo.setId(idGerado);
            return salvo;
        });

        ResponseLoteDTO response = loteService.criarLote(dto);

        assertEquals(idGerado, response.id());
        assertEquals(notaFiscalId, response.notaFiscalId());
        assertEquals("chave-1", response.notaFiscalChaveAcesso());
        assertEquals("Fornecedor A", response.fornecedor());
        assertEquals(dataEntrega, response.dataEntrega());
    }

    @Test
    void deveCriarLoteSemDataEntrega() {
        UUID notaFiscalId = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(notaFiscalId).chaveAcesso("chave-1").build();
        RequestCriarLoteDTO dto = new RequestCriarLoteDTO(notaFiscalId, "Fornecedor A", null);

        when(notaFiscalRepository.findById(notaFiscalId)).thenReturn(Optional.of(notaFiscal));
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseLoteDTO response = loteService.criarLote(dto);

        assertNull(response.dataEntrega());
    }

    @Test
    void deveLancarExcecaoQuandoNotaFiscalNaoExisteAoCriarLote() {
        UUID notaFiscalId = UUID.randomUUID();
        RequestCriarLoteDTO dto = new RequestCriarLoteDTO(notaFiscalId, "Fornecedor A", null);

        when(notaFiscalRepository.findById(notaFiscalId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> loteService.criarLote(dto));

        assertTrue(exception.getMessage().contains(notaFiscalId.toString()));
        verify(loteRepository, never()).save(any());
    }

    @Test
    void deveSalvarLoteComOsCamposCorretosAoCriar() {
        UUID notaFiscalId = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(notaFiscalId).chaveAcesso("chave-1").build();
        RequestCriarLoteDTO dto = new RequestCriarLoteDTO(notaFiscalId, "Fornecedor B", null);

        when(notaFiscalRepository.findById(notaFiscalId)).thenReturn(Optional.of(notaFiscal));
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loteService.criarLote(dto);

        ArgumentCaptor<Lote> captor = ArgumentCaptor.forClass(Lote.class);
        verify(loteRepository).save(captor.capture());

        assertEquals(notaFiscal, captor.getValue().getNotaFiscal());
        assertEquals("Fornecedor B", captor.getValue().getFornecedor());
    }

    @Test
    void deveBuscarTodosLotesPaginado() {
        NotaFiscal notaFiscal = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-1").build();
        Lote lote1 = Lote.builder().id(UUID.randomUUID()).notaFiscal(notaFiscal).fornecedor("Fornecedor A").build();
        Lote lote2 = Lote.builder().id(UUID.randomUUID()).notaFiscal(notaFiscal).fornecedor("Fornecedor B").build();

        Pageable pageable = PageRequest.of(0, 10);
        when(loteRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(lote1, lote2), pageable, 2));

        var resultado = loteService.buscarTodosLotes(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals("Fornecedor A", resultado.getContent().get(0).fornecedor());
        assertEquals("Fornecedor B", resultado.getContent().get(1).fornecedor());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaLotes() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loteRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = loteService.buscarTodosLotes(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarLotePorId() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-1").build();
        Lote lote = Lote.builder().id(id).notaFiscal(notaFiscal).fornecedor("Fornecedor A").build();

        when(loteRepository.findById(id)).thenReturn(Optional.of(lote));

        ResponseLoteDTO response = loteService.buscarLote(id);

        assertEquals(id, response.id());
        assertEquals("Fornecedor A", response.fornecedor());
    }

    @Test
    void deveLancarExcecaoQuandoLoteNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(loteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> loteService.buscarLote(id));
    }

    @Test
    void deveAtualizarLoteComSucesso() {
        UUID id = UUID.randomUUID();
        UUID novaNotaFiscalId = UUID.randomUUID();

        NotaFiscal notaFiscalAntiga = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-antiga").build();
        NotaFiscal notaFiscalNova = NotaFiscal.builder().id(novaNotaFiscalId).chaveAcesso("chave-nova").build();
        Lote loteExistente = Lote.builder().id(id).notaFiscal(notaFiscalAntiga).fornecedor("Fornecedor A").build();

        LocalDateTime dataEntrega = LocalDateTime.of(2025, 7, 1, 10, 0);
        RequestAtualizarLoteDTO dto = new RequestAtualizarLoteDTO(novaNotaFiscalId, "Fornecedor Atualizado", dataEntrega);

        when(loteRepository.findById(id)).thenReturn(Optional.of(loteExistente));
        when(notaFiscalRepository.findById(novaNotaFiscalId)).thenReturn(Optional.of(notaFiscalNova));
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseLoteDTO response = loteService.atualizarLote(id, dto);

        assertEquals(novaNotaFiscalId, response.notaFiscalId());
        assertEquals("chave-nova", response.notaFiscalChaveAcesso());
        assertEquals("Fornecedor Atualizado", response.fornecedor());
        assertEquals(dataEntrega, response.dataEntrega());
    }

    @Test
    void deveLancarExcecaoQuandoLoteNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        RequestAtualizarLoteDTO dto = new RequestAtualizarLoteDTO(UUID.randomUUID(), "Fornecedor A", null);

        when(loteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> loteService.atualizarLote(id, dto));
        verify(notaFiscalRepository, never()).findById(any());
        verify(loteRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNovaNotaFiscalNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        UUID notaFiscalId = UUID.randomUUID();

        NotaFiscal notaFiscalAntiga = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-antiga").build();
        Lote loteExistente = Lote.builder().id(id).notaFiscal(notaFiscalAntiga).fornecedor("Fornecedor A").build();
        RequestAtualizarLoteDTO dto = new RequestAtualizarLoteDTO(notaFiscalId, "Fornecedor A", null);

        when(loteRepository.findById(id)).thenReturn(Optional.of(loteExistente));
        when(notaFiscalRepository.findById(notaFiscalId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> loteService.atualizarLote(id, dto));

        assertTrue(exception.getMessage().contains(notaFiscalId.toString()));
        verify(loteRepository, never()).save(any());
    }
}