package com.six_m.uniform.domain;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.aluno.AlunoRepository;
import com.six_m.uniform.domain.pedido.Pedido;
import com.six_m.uniform.domain.pedido.PedidoRepository;
import com.six_m.uniform.domain.pedido.PedidoService;
import com.six_m.uniform.domain.pedido.dto.RequestAtualizarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.RequestCriarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.ResponsePedidoDTO;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeRepository;
import com.six_m.uniform.domain.usuario.Usuario;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock private PedidoUniformeRepository pedidoUniformeRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void deveCriarPedidoComSucesso() {
        UUID alunoId = UUID.randomUUID();
        Aluno aluno = Aluno.builder().id(alunoId).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        LocalDateTime dataEfetivada = LocalDateTime.of(2025, 6, 17, 14, 30);
        RequestCriarPedidoDTO dto = new RequestCriarPedidoDTO(alunoId, dataEfetivada);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        UUID idGerado = UUID.randomUUID();
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido salvo = invocation.getArgument(0);
            salvo.setId(idGerado);
            return salvo;
        });

        ResponsePedidoDTO response = pedidoService.criarPedido(dto, usuario);

        assertEquals(idGerado, response.id());
        assertEquals(alunoId, response.alunoId());
        assertEquals("João", response.alunoNome());
        assertEquals(usuario.getId(), response.usuarioId());
        assertEquals("Rafael", response.usuarioNome());
        assertEquals(dataEfetivada, response.dataEfetivada());
    }

    @Test
    void deveCriarPedidoSemDataEfetivada() {
        UUID alunoId = UUID.randomUUID();
        Aluno aluno = Aluno.builder().id(alunoId).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        RequestCriarPedidoDTO dto = new RequestCriarPedidoDTO(alunoId, null);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponsePedidoDTO response = pedidoService.criarPedido(dto, usuario);

        assertNull(response.dataEfetivada());
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExisteAoCriarPedido() {
        UUID alunoId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        RequestCriarPedidoDTO dto = new RequestCriarPedidoDTO(alunoId, null);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pedidoService.criarPedido(dto, usuario));

        assertTrue(exception.getMessage().contains(alunoId.toString()));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveSalvarPedidoComOsCamposCorretosAoCriar() {
        UUID alunoId = UUID.randomUUID();
        Aluno aluno = Aluno.builder().id(alunoId).nome("Maria").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        RequestCriarPedidoDTO dto = new RequestCriarPedidoDTO(alunoId, null);

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pedidoService.criarPedido(dto, usuario);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());

        assertEquals(aluno, captor.getValue().getAluno());
        assertEquals(usuario, captor.getValue().getUsuario());
    }

    @Test
    void deveBuscarTodosPedidosPaginado() {
        Aluno aluno = Aluno.builder().id(UUID.randomUUID()).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        Pedido pedido1 = Pedido.builder().id(UUID.randomUUID()).aluno(aluno).usuario(usuario).build();
        Pedido pedido2 = Pedido.builder().id(UUID.randomUUID()).aluno(aluno).usuario(usuario).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(pedidoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(pedido1, pedido2), pageable, 2));

        var resultado = pedidoService.buscarTodosPedidos(pageable);

        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaPedidos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pedidoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = pedidoService.buscarTodosPedidos(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarPedidoPorId() {
        UUID id = UUID.randomUUID();
        Aluno aluno = Aluno.builder().id(UUID.randomUUID()).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        Pedido pedido = Pedido.builder().id(id).aluno(aluno).usuario(usuario).build();

        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedido));

        ResponsePedidoDTO response = pedidoService.buscarPedido(id);

        assertEquals(id, response.id());
        assertEquals("João", response.alunoNome());
        assertEquals("Rafael", response.usuarioNome());
    }

    @Test
    void deveLancarExcecaoQuandoPedidoNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(pedidoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pedidoService.buscarPedido(id));
    }

    @Test
    void deveAtualizarPedidoComSucesso() {
        UUID id = UUID.randomUUID();
        UUID novoAlunoId = UUID.randomUUID();

        Aluno alunoAntigo = Aluno.builder().id(UUID.randomUUID()).nome("João").build();
        Aluno alunoNovo = Aluno.builder().id(novoAlunoId).nome("Maria").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        Pedido pedidoExistente = Pedido.builder().id(id).aluno(alunoAntigo).usuario(usuario).build();

        LocalDateTime novaData = LocalDateTime.of(2025, 7, 1, 10, 0);
        RequestAtualizarPedidoDTO dto = new RequestAtualizarPedidoDTO(novoAlunoId, novaData);

        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedidoExistente));
        when(alunoRepository.findById(novoAlunoId)).thenReturn(Optional.of(alunoNovo));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponsePedidoDTO response = pedidoService.atualizarPedido(id, dto);

        assertEquals(novoAlunoId, response.alunoId());
        assertEquals("Maria", response.alunoNome());
        assertEquals(novaData, response.dataEfetivada());
        assertEquals("Rafael", response.usuarioNome());
    }

    @Test
    void deveLancarExcecaoQuandoPedidoNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        RequestAtualizarPedidoDTO dto = new RequestAtualizarPedidoDTO(UUID.randomUUID(), null);

        when(pedidoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pedidoService.atualizarPedido(id, dto));
        verify(alunoRepository, never()).findById(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNovoAlunoNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();

        Aluno alunoAntigo = Aluno.builder().id(UUID.randomUUID()).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        Pedido pedidoExistente = Pedido.builder().id(id).aluno(alunoAntigo).usuario(usuario).build();
        RequestAtualizarPedidoDTO dto = new RequestAtualizarPedidoDTO(alunoId, null);

        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedidoExistente));
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pedidoService.atualizarPedido(id, dto));

        assertTrue(exception.getMessage().contains(alunoId.toString()));
        verify(pedidoRepository, never()).save(any());
    }
}