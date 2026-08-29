package com.six_m.uniform.domain;

import com.six_m.uniform.domain.notaFiscal.NotaFiscal;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalRepository;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalService;
import com.six_m.uniform.domain.notaFiscal.dto.ResponseNotaFiscalDTO;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class NotaFiscalServiceTest {

    @Mock
    private NotaFiscalRepository notaFiscalRepository;

    @InjectMocks
    private NotaFiscalService notaFiscalService;

    @Test
    void deveCriarNotaFiscalParaLoteComSucesso() {
        when(notaFiscalRepository.existsByChaveAcesso("chave-1")).thenReturn(false);
        when(notaFiscalRepository.save(any(NotaFiscal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotaFiscal resultado = notaFiscalService.criarParaLote("chave-1");

        assertEquals("chave-1", resultado.getChaveAcesso());
    }

    @Test
    void deveLancarExcecaoQuandoChaveAcessoJaExisteAoCriarParaLote() {
        when(notaFiscalRepository.existsByChaveAcesso("chave-1")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> notaFiscalService.criarParaLote("chave-1"));

        assertEquals("Já existe uma nota fiscal com esta chave de acesso", exception.getMessage());
        verify(notaFiscalRepository, never()).save(any());
    }

    @Test
    void deveBuscarTodasNotasFiscaisPaginado() {
        NotaFiscal nf1 = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-1").build();
        NotaFiscal nf2 = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-2").build();

        Pageable pageable = PageRequest.of(0, 10);
        when(notaFiscalRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(nf1, nf2), pageable, 2));

        var resultado = notaFiscalService.buscarTodasNotasFiscais(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals("chave-1", resultado.getContent().get(0).chaveAcesso());
        assertEquals("chave-2", resultado.getContent().get(1).chaveAcesso());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaNotasFiscais() {
        Pageable pageable = PageRequest.of(0, 10);
        when(notaFiscalRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = notaFiscalService.buscarTodasNotasFiscais(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarNotaFiscalPorId() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(id).chaveAcesso("chave-1").build();

        when(notaFiscalRepository.findById(id)).thenReturn(Optional.of(notaFiscal));

        ResponseNotaFiscalDTO response = notaFiscalService.buscarNotaFiscal(id);

        assertEquals(id, response.id());
        assertEquals("chave-1", response.chaveAcesso());
    }

    @Test
    void deveLancarExcecaoQuandoNotaFiscalNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(notaFiscalRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> notaFiscalService.buscarNotaFiscal(id));

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void deveAtualizarNotaFiscalParaLoteComSucesso() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(id).chaveAcesso("chave-antiga").build();

        when(notaFiscalRepository.existsByChaveAcessoAndIdNot("chave-nova", id)).thenReturn(false);
        when(notaFiscalRepository.save(any(NotaFiscal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotaFiscal resultado = notaFiscalService.atualizarParaLote(notaFiscal, "chave-nova");

        assertEquals("chave-nova", resultado.getChaveAcesso());
    }

    @Test
    void devePermitirAtualizarNotaFiscalParaLoteMantendoAMesmaChave() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(id).chaveAcesso("chave-1").build();

        when(notaFiscalRepository.save(any(NotaFiscal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notaFiscalService.atualizarParaLote(notaFiscal, "chave-1");

        verify(notaFiscalRepository, never()).existsByChaveAcessoAndIdNot(any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoNovaChaveJaPertenceAOutraNotaFiscalAoAtualizarParaLote() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(id).chaveAcesso("chave-antiga").build();

        when(notaFiscalRepository.existsByChaveAcessoAndIdNot("chave-de-outra", id)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> notaFiscalService.atualizarParaLote(notaFiscal, "chave-de-outra"));

        assertEquals("Já existe uma nota fiscal com esta chave de acesso", exception.getMessage());
        verify(notaFiscalRepository, never()).save(any());
    }

}