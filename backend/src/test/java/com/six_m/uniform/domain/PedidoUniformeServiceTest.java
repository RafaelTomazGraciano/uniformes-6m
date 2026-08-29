package com.six_m.uniform.domain;

import com.six_m.uniform.domain.pedido.Pedido;
import com.six_m.uniform.domain.pedido.dto.RequestItemSaidaDTO;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeRepository;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeService;
import com.six_m.uniform.domain.pedidoUniforme.dto.ResponsePedidoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeService;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoUniformeServiceTest {

    @Mock
    private PedidoUniformeRepository pedidoUniformeRepository;

    @Mock
    private UniformeService uniformeService;

    @InjectMocks
    private PedidoUniformeService pedidoUniformeService;

    @Test
    void deveBuscarTodosPedidosUniformePaginado() {
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipoUniforme).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();

        PedidoUniforme item1 = PedidoUniforme.builder().id(UUID.randomUUID()).pedido(pedido).uniforme(uniforme).quantidade(1).build();
        PedidoUniforme item2 = PedidoUniforme.builder().id(UUID.randomUUID()).pedido(pedido).uniforme(uniforme).quantidade(2).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(pedidoUniformeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(item1, item2), pageable, 2));

        var resultado = pedidoUniformeService.buscarTodosPedidosUniforme(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().get(0).quantidade());
        assertEquals(2, resultado.getContent().get(1).quantidade());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaPedidosUniforme() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pedidoUniformeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = pedidoUniformeService.buscarTodosPedidosUniforme(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarPedidoUniformePorId() {
        UUID id = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipoUniforme).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        PedidoUniforme pedidoUniforme = PedidoUniforme.builder().id(id).pedido(pedido).uniforme(uniforme).quantidade(4).build();

        when(pedidoUniformeRepository.findById(id)).thenReturn(Optional.of(pedidoUniforme));

        ResponsePedidoUniformeDTO response = pedidoUniformeService.buscarPedidoUniforme(id);

        assertEquals(id, response.id());
        assertEquals(4, response.quantidade());
    }

    @Test
    void deveLancarExcecaoQuandoPedidoUniformeNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(pedidoUniformeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pedidoUniformeService.buscarPedidoUniforme(id));
    }

    @Test
    void deveCriarItensParaPedidoComSucesso() {
        UUID uniformeId = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().id(uniformeId).tipoUniforme(tipoUniforme).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        RequestItemSaidaDTO itemDto = new RequestItemSaidaDTO(uniformeId, 3);

        when(uniformeService.buscarUniformeEntidade(uniformeId)).thenReturn(uniforme);
        when(pedidoUniformeRepository.save(any(PedidoUniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<PedidoUniforme> resultado = pedidoUniformeService.criarItensParaPedido(pedido, List.of(itemDto));

        assertEquals(1, resultado.size());
        assertEquals(uniforme, resultado.getFirst().getUniforme());
        assertEquals(3, resultado.getFirst().getQuantidade());
    }

    @Test
    void deveLancarExcecaoQuandoHaItensDuplicadosNoPedido() {
        UUID uniformeId = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();
        RequestItemSaidaDTO item1 = new RequestItemSaidaDTO(uniformeId, 2);
        RequestItemSaidaDTO item2 = new RequestItemSaidaDTO(uniformeId, 5);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> pedidoUniformeService.criarItensParaPedido(pedido, List.of(item1, item2)));

        assertEquals("Item duplicado no pedido: mesmo uniforme informado mais de uma vez", exception.getMessage());
        verify(pedidoUniformeRepository, never()).save(any());
    }

    @Test
    void devePermitirItensComUniformesDiferentesNoMesmoPedido() {
        UUID uniformeId1 = UUID.randomUUID();
        UUID uniformeId2 = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme1 = Uniforme.builder().id(uniformeId1).tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).build();
        Uniforme uniforme2 = Uniforme.builder().id(uniformeId2).tipoUniforme(tipo).tamanho(Tamanho.G).sexo(Sexo.FEMININO).build();

        when(uniformeService.buscarUniformeEntidade(uniformeId1)).thenReturn(uniforme1);
        when(uniformeService.buscarUniformeEntidade(uniformeId2)).thenReturn(uniforme2);
        when(pedidoUniformeRepository.save(any(PedidoUniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<PedidoUniforme> resultado = pedidoUniformeService.criarItensParaPedido(pedido,
                List.of(new RequestItemSaidaDTO(uniformeId1, 2), new RequestItemSaidaDTO(uniformeId2, 4)));

        assertEquals(2, resultado.size());
    }

    @Test
    void deveLancarExcecaoQuandoUniformeNaoExisteAoCriarItensParaPedido() {
        UUID uniformeId = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();
        RequestItemSaidaDTO itemDto = new RequestItemSaidaDTO(uniformeId, 3);

        when(uniformeService.buscarUniformeEntidade(uniformeId))
                .thenThrow(new NotFoundException("Uniforme não encontrado com o ID: " + uniformeId));

        assertThrows(NotFoundException.class,
                () -> pedidoUniformeService.criarItensParaPedido(pedido, List.of(itemDto)));

        verify(pedidoUniformeRepository, never()).save(any());
    }

    @Test
    void deveBuscarItensPorPedido() {
        UUID pedidoId = UUID.randomUUID();
        PedidoUniforme item = PedidoUniforme.builder().id(UUID.randomUUID()).build();

        when(pedidoUniformeRepository.findByPedidoId(pedidoId)).thenReturn(List.of(item));

        List<PedidoUniforme> resultado = pedidoUniformeService.buscarItensPorPedido(pedidoId);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveDeletarItensPorPedido() {
        PedidoUniforme item1 = PedidoUniforme.builder().id(UUID.randomUUID()).build();
        PedidoUniforme item2 = PedidoUniforme.builder().id(UUID.randomUUID()).build();

        pedidoUniformeService.deletarItensPorPedido(List.of(item1, item2));

        verify(pedidoUniformeRepository).deleteAll(List.of(item1, item2));
    }
}