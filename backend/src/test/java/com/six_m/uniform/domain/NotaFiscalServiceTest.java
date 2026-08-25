package com.six_m.uniform.domain;

import com.six_m.uniform.domain.lote.LoteRepository;
import com.six_m.uniform.domain.notaFiscal.NotaFiscal;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalRepository;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalService;
import com.six_m.uniform.domain.notaFiscal.dto.RequestAtualizarNotaFiscalDTO;
import com.six_m.uniform.domain.notaFiscal.dto.RequestCriarNotaFiscalDTO;
import com.six_m.uniform.domain.notaFiscal.dto.ResponseNotaFiscalDTO;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
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

    @Mock
    private LoteRepository loteRepository;

    @InjectMocks
    private NotaFiscalService notaFiscalService;

    @Test
    void deveCriarNotaFiscalComSucesso() {
        RequestCriarNotaFiscalDTO dto = new RequestCriarNotaFiscalDTO("12345678901234567890123456789012345678901234");

        when(notaFiscalRepository.existsByChaveAcesso(dto.chaveAcesso())).thenReturn(false);

        UUID idGerado = UUID.randomUUID();
        when(notaFiscalRepository.save(any(NotaFiscal.class))).thenAnswer(invocation -> {
            NotaFiscal salvo = invocation.getArgument(0);
            salvo.setId(idGerado);
            return salvo;
        });

        ResponseNotaFiscalDTO response = notaFiscalService.criarNotaFiscal(dto);

        assertEquals(idGerado, response.id());
        assertEquals(dto.chaveAcesso(), response.chaveAcesso());
    }

    @Test
    void deveLancarExcecaoQuandoChaveAcessoJaExisteAoCriar() {
        RequestCriarNotaFiscalDTO dto = new RequestCriarNotaFiscalDTO("chave-existente");

        when(notaFiscalRepository.existsByChaveAcesso(dto.chaveAcesso())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> notaFiscalService.criarNotaFiscal(dto));

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
    void deveAtualizarNotaFiscalComSucesso() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscalExistente = NotaFiscal.builder().id(id).chaveAcesso("chave-antiga").build();
        RequestAtualizarNotaFiscalDTO dto = new RequestAtualizarNotaFiscalDTO("chave-nova");

        when(notaFiscalRepository.findById(id)).thenReturn(Optional.of(notaFiscalExistente));
        when(notaFiscalRepository.existsByChaveAcesso("chave-nova")).thenReturn(false);
        when(notaFiscalRepository.save(any(NotaFiscal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseNotaFiscalDTO response = notaFiscalService.atualizarNotaFiscal(id, dto);

        assertEquals("chave-nova", response.chaveAcesso());
    }

    @Test
    void devePermitirAtualizarNotaFiscalMantendoAMesmaChave() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscalExistente = NotaFiscal.builder().id(id).chaveAcesso("chave-1").build();
        RequestAtualizarNotaFiscalDTO dto = new RequestAtualizarNotaFiscalDTO("chave-1");

        when(notaFiscalRepository.findById(id)).thenReturn(Optional.of(notaFiscalExistente));
        when(notaFiscalRepository.save(any(NotaFiscal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseNotaFiscalDTO response = notaFiscalService.atualizarNotaFiscal(id, dto);

        assertEquals("chave-1", response.chaveAcesso());
        verify(notaFiscalRepository, never()).existsByChaveAcesso(any());
    }

    @Test
    void deveLancarExcecaoQuandoNovaChaveJaExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscalExistente = NotaFiscal.builder().id(id).chaveAcesso("chave-antiga").build();
        RequestAtualizarNotaFiscalDTO dto = new RequestAtualizarNotaFiscalDTO("chave-de-outra-nota");

        when(notaFiscalRepository.findById(id)).thenReturn(Optional.of(notaFiscalExistente));
        when(notaFiscalRepository.existsByChaveAcesso("chave-de-outra-nota")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> notaFiscalService.atualizarNotaFiscal(id, dto));

        assertEquals("Já existe uma nota fiscal com esta chave de acesso", exception.getMessage());
        verify(notaFiscalRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNotaFiscalNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        RequestAtualizarNotaFiscalDTO dto = new RequestAtualizarNotaFiscalDTO("chave-1");

        when(notaFiscalRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> notaFiscalService.atualizarNotaFiscal(id, dto));
        verify(notaFiscalRepository, never()).save(any());
    }

    @Test
    void deveDeletarNotaFiscalComSucessoQuandoNaoHaLotesVinculados() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(id).chaveAcesso("chave-1").build();

        when(notaFiscalRepository.findById(id)).thenReturn(Optional.of(notaFiscal));
        when(loteRepository.existsByNotaFiscalId(id)).thenReturn(false);

        MessageResponseDTO resultado = notaFiscalService.deletarNotaFiscal(id);

        assertEquals("Nota fiscal deletada com sucesso", resultado.message());
        verify(notaFiscalRepository).delete(notaFiscal);
    }

    @Test
    void deveLancarExcecaoAoDeletarNotaFiscalComLotesVinculados() {
        UUID id = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(id).chaveAcesso("chave-1").build();

        when(notaFiscalRepository.findById(id)).thenReturn(Optional.of(notaFiscal));
        when(loteRepository.existsByNotaFiscalId(id)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> notaFiscalService.deletarNotaFiscal(id));

        assertEquals("Não é possível excluir a nota fiscal: existem lotes vinculados a ela", exception.getMessage());
        verify(notaFiscalRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoQuandoNotaFiscalNaoExisteAoDeletar() {
        UUID id = UUID.randomUUID();
        when(notaFiscalRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> notaFiscalService.deletarNotaFiscal(id));
        verify(loteRepository, never()).existsByNotaFiscalId(any());
    }
}