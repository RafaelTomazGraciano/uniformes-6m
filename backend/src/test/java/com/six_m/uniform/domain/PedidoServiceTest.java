package com.six_m.uniform.domain;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.aluno.AlunoRepository;
import com.six_m.uniform.domain.pedido.Pedido;
import com.six_m.uniform.domain.pedido.PedidoRepository;
import com.six_m.uniform.domain.pedido.PedidoService;
import com.six_m.uniform.domain.pedido.dto.RequestAtualizarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.RequestCriarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.RequestItemSaidaDTO;
import com.six_m.uniform.domain.pedido.dto.ResponsePedidoDTO;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeService;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.domain.usuario.Usuario;
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

import java.util.List;
import java.util.Map;
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

    @Mock
    private PedidoUniformeService pedidoUniformeService;

    @Mock
    private UniformeService uniformeService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void deveCriarPedidoComSucesso() {
        UUID alunoId = UUID.randomUUID();
        UUID uniformeId = UUID.randomUUID();
        Aluno aluno = Aluno.builder().id(alunoId).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        RequestItemSaidaDTO itemDto = new RequestItemSaidaDTO(uniformeId, 3);
        RequestCriarPedidoDTO dto = new RequestCriarPedidoDTO(alunoId, null, List.of(itemDto));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido salvo = invocation.getArgument(0);
            salvo.setId(pedidoId);
            return salvo;
        });

        TipoUniforme tipoUniforme = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().id(uniformeId).tipoUniforme(tipoUniforme).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        PedidoUniforme itemSalvo = PedidoUniforme.builder().id(UUID.randomUUID()).uniforme(uniforme).quantidade(3).build();
        when(pedidoUniformeService.criarItensParaPedido(any(Pedido.class), eq(dto.itens()))).thenReturn(List.of(itemSalvo));

        ResponsePedidoDTO response = pedidoService.criarPedido(dto, usuario);

        assertEquals(pedidoId, response.id());
        assertEquals(alunoId, response.alunoId());
        assertEquals(1, response.itens().size());
        verify(uniformeService).darSaida(uniformeId, 3);
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExisteAoCriarPedido() {
        UUID alunoId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        RequestCriarPedidoDTO dto = new RequestCriarPedidoDTO(alunoId, null, List.of(new RequestItemSaidaDTO(UUID.randomUUID(), 2)));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pedidoService.criarPedido(dto, usuario));

        assertTrue(exception.getMessage().contains(alunoId.toString()));
        verify(pedidoRepository, never()).save(any());
        verify(pedidoUniformeService, never()).criarItensParaPedido(any(), any());
    }

    @Test
    void devePropagarExcecaoQuandoEstoqueInsuficienteAoCriarPedido() {
        UUID alunoId = UUID.randomUUID();
        UUID uniformeId = UUID.randomUUID();
        Aluno aluno = Aluno.builder().id(alunoId).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        RequestCriarPedidoDTO dto = new RequestCriarPedidoDTO(alunoId, null, List.of(new RequestItemSaidaDTO(uniformeId, 100)));

        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().id(uniformeId).tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        PedidoUniforme item = PedidoUniforme.builder().id(UUID.randomUUID()).uniforme(uniforme).quantidade(100).build();
        when(pedidoUniformeService.criarItensParaPedido(any(Pedido.class), eq(dto.itens()))).thenReturn(List.of(item));

        doThrow(new BadRequestException("Quantidade solicitada (100) maior que o estoque disponível (5)"))
                .when(uniformeService).darSaida(uniformeId, 100);

        assertThrows(BadRequestException.class, () -> pedidoService.criarPedido(dto, usuario));
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
    void deveAtualizarPedidoEstornandoItensAntigosEDandoSaidaNosNovos() {
        UUID pedidoId = UUID.randomUUID();
        UUID novoAlunoId = UUID.randomUUID();
        UUID uniformeAntigoId = UUID.randomUUID();
        UUID uniformeNovoId = UUID.randomUUID();

        Aluno alunoAntigo = Aluno.builder().id(UUID.randomUUID()).nome("João").build();
        Aluno alunoNovo = Aluno.builder().id(novoAlunoId).nome("Maria").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        Pedido pedidoExistente = Pedido.builder().id(pedidoId).aluno(alunoAntigo).usuario(usuario).build();

        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniformeAntigo = Uniforme.builder().id(uniformeAntigoId).tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        PedidoUniforme itemAntigo = PedidoUniforme.builder().id(UUID.randomUUID()).uniforme(uniformeAntigo).quantidade(4).build();

        RequestItemSaidaDTO itemNovoDto = new RequestItemSaidaDTO(uniformeNovoId, 2);
        RequestAtualizarPedidoDTO dto = new RequestAtualizarPedidoDTO(novoAlunoId, null, List.of(itemNovoDto));

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedidoExistente));
        when(alunoRepository.findById(novoAlunoId)).thenReturn(Optional.of(alunoNovo));
        when(pedidoUniformeService.buscarItensPorPedido(pedidoId)).thenReturn(List.of(itemAntigo));

        Uniforme uniformeNovo = Uniforme.builder().id(uniformeNovoId).tipoUniforme(tipo).tamanho(Tamanho.G).sexo(Sexo.FEMININO).build();
        PedidoUniforme itemNovo = PedidoUniforme.builder().id(UUID.randomUUID()).uniforme(uniformeNovo).quantidade(2).build();
        when(pedidoUniformeService.criarItensParaPedido(pedidoExistente, dto.itens())).thenReturn(List.of(itemNovo));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponsePedidoDTO response = pedidoService.atualizarPedido(pedidoId, dto);

        verify(uniformeService).estornarSaida(uniformeAntigoId, 4);
        verify(pedidoUniformeService).deletarItensPorPedido(List.of(itemAntigo));
        verify(uniformeService).darSaida(uniformeNovoId, 2);
        assertEquals(novoAlunoId, response.alunoId());
    }

    @Test
    void deveLancarExcecaoQuandoPedidoNaoExisteAoAtualizar() {
        UUID pedidoId = UUID.randomUUID();
        RequestAtualizarPedidoDTO dto = new RequestAtualizarPedidoDTO(UUID.randomUUID(), null, List.of(new RequestItemSaidaDTO(UUID.randomUUID(), 2)));

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pedidoService.atualizarPedido(pedidoId, dto));
        verify(alunoRepository, never()).findById(any());
        verify(pedidoUniformeService, never()).buscarItensPorPedido(any());
    }

    @Test
    void deveBuscarTodosPedidosPaginadoComItens() {
        UUID pedidoId = UUID.randomUUID();
        Aluno aluno = Aluno.builder().id(UUID.randomUUID()).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        Pedido pedido = Pedido.builder().id(pedidoId).aluno(aluno).usuario(usuario).build();

        TipoUniforme tipoUniforme = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipoUniforme).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        PedidoUniforme item = PedidoUniforme.builder().id(UUID.randomUUID()).pedido(pedido).uniforme(uniforme).quantidade(3).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(pedidoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(pedido), pageable, 1));
        when(pedidoUniformeService.buscarItensPorPedidos(List.of(pedidoId))).thenReturn(Map.of(pedidoId, List.of(item)));

        var resultado = pedidoService.buscarTodosPedidos(pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().getFirst().itens().size());
        verify(pedidoUniformeService, never()).buscarItensPorPedido(any());
    }

    @Test
    void deveBuscarPedidoPorIdComItens() {
        UUID pedidoId = UUID.randomUUID();
        Aluno aluno = Aluno.builder().id(UUID.randomUUID()).nome("João").build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Rafael").email("rafael@teste.com").build();
        Pedido pedido = Pedido.builder().id(pedidoId).aluno(aluno).usuario(usuario).build();

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoUniformeService.buscarItensPorPedido(pedidoId)).thenReturn(List.of());

        ResponsePedidoDTO response = pedidoService.buscarPedido(pedidoId);

        assertEquals(pedidoId, response.id());
        assertTrue(response.itens().isEmpty());
    }

}