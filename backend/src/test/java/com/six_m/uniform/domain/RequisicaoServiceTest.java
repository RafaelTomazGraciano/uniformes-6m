package com.six_m.uniform.domain;

import com.six_m.uniform.domain.requisicao.Requisicao;
import com.six_m.uniform.domain.requisicao.RequisicaoRepository;
import com.six_m.uniform.domain.requisicao.RequisicaoService;
import com.six_m.uniform.domain.requisicao.dto.RequestCriarRequisicaoDTO;
import com.six_m.uniform.domain.requisicao.dto.ResponseRequisicaoDTO;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeService;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.StatusRequisicao;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequisicaoServiceTest {

    @Mock
    private RequisicaoRepository requisicaoRepository;

    @Mock
    private TipoUniformeService tipoUniformeService;

    @InjectMocks
    private RequisicaoService requisicaoService;

    private final TipoUniforme camiseta = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").estoqueBaixo(5).build();
    private final Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();

    private Requisicao requisicaoCom(StatusRequisicao status) {
        return Requisicao.builder()
                .id(UUID.randomUUID())
                .tipoUniforme(camiseta)
                .tamanho(Tamanho.P)
                .sexo(Sexo.MASCULINO)
                .quantidade(10)
                .status(status)
                .usuario(usuario)
                .dataCriacao(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void deveCriarRequisicaoPendenteComSolicitanteDoToken() {
        RequestCriarRequisicaoDTO dto = new RequestCriarRequisicaoDTO(camiseta.getId(), Tamanho.P, Sexo.MASCULINO, 20, "  Faltando para o 6º ano  ");

        when(tipoUniformeService.buscarTipoUniformeEntidade(camiseta.getId())).thenReturn(camiseta);
        UUID requisicaoId = UUID.randomUUID();
        when(requisicaoRepository.save(any(Requisicao.class))).thenAnswer(invocation -> {
            Requisicao salva = invocation.getArgument(0);
            salva.setId(requisicaoId);
            return salva;
        });

        ResponseRequisicaoDTO response = requisicaoService.criarRequisicao(dto, usuario);

        assertEquals(requisicaoId, response.id());
        assertEquals("Camiseta", response.tipoUniforme());
        assertEquals(Tamanho.P, response.tamanho());
        assertEquals(Sexo.MASCULINO, response.sexo());
        assertEquals(20, response.quantidade());
        assertEquals("Faltando para o 6º ano", response.observacao());
        assertEquals(StatusRequisicao.PENDENTE, response.status());
        assertEquals(usuario.getId(), response.usuarioId());
        assertEquals("Rafael", response.usuarioNome());
        assertNotNull(response.dataCriacao());
        assertNull(response.dataConclusao());
    }

    @Test
    void deveGuardarObservacaoEmBrancoComoNula() {
        RequestCriarRequisicaoDTO dto = new RequestCriarRequisicaoDTO(camiseta.getId(), Tamanho.M, Sexo.FEMININO, 5, "   ");

        when(tipoUniformeService.buscarTipoUniformeEntidade(camiseta.getId())).thenReturn(camiseta);
        when(requisicaoRepository.save(any(Requisicao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requisicaoService.criarRequisicao(dto, usuario);

        ArgumentCaptor<Requisicao> captor = ArgumentCaptor.forClass(Requisicao.class);
        verify(requisicaoRepository).save(captor.capture());
        assertNull(captor.getValue().getObservacao());
    }

    @Test
    void deveLancarExcecaoQuandoTipoUniformeNaoExisteAoCriarRequisicao() {
        UUID tipoId = UUID.randomUUID();
        RequestCriarRequisicaoDTO dto = new RequestCriarRequisicaoDTO(tipoId, Tamanho.P, Sexo.MASCULINO, 3, null);

        when(tipoUniformeService.buscarTipoUniformeEntidade(tipoId))
                .thenThrow(new NotFoundException("Tipo de uniforme não encontrado com o ID: " + tipoId));

        assertThrows(NotFoundException.class, () -> requisicaoService.criarRequisicao(dto, usuario));
        verify(requisicaoRepository, never()).save(any());
    }

    @Test
    void deveListarTodasQuandoNaoHaFiltroDeStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(requisicaoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(requisicaoCom(StatusRequisicao.PENDENTE), requisicaoCom(StatusRequisicao.ATENDIDA)), pageable, 2));

        var resultado = requisicaoService.buscarTodasRequisicoes(null, pageable);

        assertEquals(2, resultado.getTotalElements());
        verify(requisicaoRepository, never()).findByStatusIn(any(), any());
    }

    @Test
    void deveFiltrarRequisicoesEmAndamento() {
        Pageable pageable = PageRequest.of(0, 10);
        List<StatusRequisicao> status = List.of(StatusRequisicao.PENDENTE);
        when(requisicaoRepository.findByStatusIn(status, pageable))
                .thenReturn(new PageImpl<>(List.of(requisicaoCom(StatusRequisicao.PENDENTE)), pageable, 1));

        var resultado = requisicaoService.buscarTodasRequisicoes(status, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(StatusRequisicao.PENDENTE, resultado.getContent().getFirst().status());
        verify(requisicaoRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void deveBuscarRequisicaoPorId() {
        Requisicao requisicao = requisicaoCom(StatusRequisicao.PENDENTE);
        when(requisicaoRepository.findById(requisicao.getId())).thenReturn(Optional.of(requisicao));

        ResponseRequisicaoDTO response = requisicaoService.buscarRequisicao(requisicao.getId());

        assertEquals(requisicao.getId(), response.id());
    }

    @Test
    void deveLancarExcecaoQuandoRequisicaoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(requisicaoRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> requisicaoService.buscarRequisicao(id));

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void deveAtenderRequisicaoPendente() {
        Requisicao requisicao = requisicaoCom(StatusRequisicao.PENDENTE);
        when(requisicaoRepository.findById(requisicao.getId())).thenReturn(Optional.of(requisicao));
        when(requisicaoRepository.save(any(Requisicao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseRequisicaoDTO response = requisicaoService.atenderRequisicao(requisicao.getId());

        assertEquals(StatusRequisicao.ATENDIDA, response.status());
        assertNotNull(response.dataConclusao());
    }

    @Test
    void deveCancelarRequisicaoPendente() {
        Requisicao requisicao = requisicaoCom(StatusRequisicao.PENDENTE);
        when(requisicaoRepository.findById(requisicao.getId())).thenReturn(Optional.of(requisicao));
        when(requisicaoRepository.save(any(Requisicao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseRequisicaoDTO response = requisicaoService.cancelarRequisicao(requisicao.getId());

        assertEquals(StatusRequisicao.CANCELADA, response.status());
        assertNotNull(response.dataConclusao());
    }

    @Test
    void naoDeveAtenderRequisicaoJaCancelada() {
        Requisicao requisicao = requisicaoCom(StatusRequisicao.CANCELADA);
        when(requisicaoRepository.findById(requisicao.getId())).thenReturn(Optional.of(requisicao));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> requisicaoService.atenderRequisicao(requisicao.getId()));

        assertEquals("Só é possível atender uma requisição pendente. Esta requisição já está cancelada", exception.getMessage());
        verify(requisicaoRepository, never()).save(any());
    }

    @Test
    void naoDeveCancelarRequisicaoJaAtendida() {
        Requisicao requisicao = requisicaoCom(StatusRequisicao.ATENDIDA);
        when(requisicaoRepository.findById(requisicao.getId())).thenReturn(Optional.of(requisicao));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> requisicaoService.cancelarRequisicao(requisicao.getId()));

        assertEquals("Só é possível cancelar uma requisição pendente. Esta requisição já está atendida", exception.getMessage());
        verify(requisicaoRepository, never()).save(any());
    }
}
