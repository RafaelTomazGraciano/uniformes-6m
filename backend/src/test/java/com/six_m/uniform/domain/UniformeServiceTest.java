package com.six_m.uniform.domain;

import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeService;
import com.six_m.uniform.domain.uniforme.CombinacaoUniforme;
import com.six_m.uniform.domain.uniforme.EstoqueView;
import com.six_m.uniform.domain.uniforme.SituacaoEstoque;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeRepository;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.domain.uniforme.dto.ResponseEstoquePainelDTO;
import com.six_m.uniform.domain.uniforme.dto.ResponseUniformeDTO;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
public class UniformeServiceTest {

    @Mock
    private UniformeRepository uniformeRepository;

    @Mock
    private TipoUniformeService tipoUniformeService;

    @InjectMocks
    private UniformeService uniformeService;

    @Test
    void deveBuscarTodosUniformesPaginado() {
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").estoqueBaixo(10).build();
        Uniforme uniforme1 = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipo).tamanho(Tamanho.P).quantidade(3).sexo(Sexo.MASCULINO).build();
        Uniforme uniforme2 = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipo).tamanho(Tamanho.G).quantidade(7).sexo(Sexo.FEMININO).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(uniformeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(uniforme1, uniforme2), pageable, 2));

        var resultado = uniformeService.buscarTodosUniformes(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals(Tamanho.P, resultado.getContent().get(0).tamanho());
        assertEquals(Tamanho.G, resultado.getContent().get(1).tamanho());
        assertEquals(10, resultado.getContent().get(0).estoqueBaixo());
        assertEquals(10, resultado.getContent().get(1).estoqueBaixo());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaUniformes() {
        Pageable pageable = PageRequest.of(0, 10);
        when(uniformeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = uniformeService.buscarTodosUniformes(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarUniformePorId() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").estoqueBaixo(10).build();
        Uniforme uniforme = Uniforme.builder().id(id).tipoUniforme(tipo).tamanho(Tamanho.M).quantidade(10).sexo(Sexo.MASCULINO).build();

        when(uniformeRepository.findById(id)).thenReturn(Optional.of(uniforme));

        ResponseUniformeDTO response = uniformeService.buscarUniforme(id);

        assertEquals(id, response.id());
        assertEquals(Tamanho.M, response.tamanho());
        assertEquals(10, response.estoqueBaixo());
    }

    @Test
    void deveLancarExcecaoQuandoUniformeNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(uniformeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> uniformeService.buscarUniforme(id));
    }

    @Test
    void deveDarEntradaSomandoQuantidadeQuandoUniformeJaExiste() {
        UUID tipoId = UUID.randomUUID();
        Uniforme existente = Uniforme.builder().id(UUID.randomUUID()).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(5).build();

        when(uniformeRepository.findByTipoUniformeIdAndTamanhoAndSexo(tipoId, Tamanho.M, Sexo.MASCULINO))
                .thenReturn(Optional.of(existente));
        when(uniformeRepository.save(any(Uniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Uniforme resultado = uniformeService.darEntrada(tipoId, Tamanho.M, Sexo.MASCULINO, 10);

        assertEquals(15, resultado.getQuantidade());
        verify(tipoUniformeService, never()).buscarTipoUniformeEntidade(any());
    }

    @Test
    void deveDarEntradaCriandoNovoUniformeQuandoNaoExiste() {
        UUID tipoId = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(tipoId).tipo("Camiseta").build();

        when(uniformeRepository.findByTipoUniformeIdAndTamanhoAndSexo(tipoId, Tamanho.M, Sexo.MASCULINO))
                .thenReturn(Optional.empty());
        when(tipoUniformeService.buscarTipoUniformeEntidade(tipoId)).thenReturn(tipoUniforme);
        when(uniformeRepository.save(any(Uniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Uniforme resultado = uniformeService.darEntrada(tipoId, Tamanho.M, Sexo.MASCULINO, 10);

        assertEquals(10, resultado.getQuantidade());
        assertEquals(tipoUniforme, resultado.getTipoUniforme());
    }

    @Test
    void deveEstornarEntradaSubtraindoQuantidade() {
        UUID tipoId = UUID.randomUUID();
        Uniforme existente = Uniforme.builder().id(UUID.randomUUID()).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(15).build();

        when(uniformeRepository.findByTipoUniformeIdAndTamanhoAndSexo(tipoId, Tamanho.M, Sexo.MASCULINO))
                .thenReturn(Optional.of(existente));
        when(uniformeRepository.save(any(Uniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        uniformeService.estornarEntrada(tipoId, Tamanho.M, Sexo.MASCULINO, 10);

        ArgumentCaptor<Uniforme> captor = ArgumentCaptor.forClass(Uniforme.class);
        verify(uniformeRepository).save(captor.capture());
        assertEquals(5, captor.getValue().getQuantidade());
    }

    @Test
    void deveLancarExcecaoAoEstornarEntradaQuandoUniformeNaoExiste() {
        UUID tipoId = UUID.randomUUID();

        when(uniformeRepository.findByTipoUniformeIdAndTamanhoAndSexo(tipoId, Tamanho.M, Sexo.MASCULINO))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> uniformeService.estornarEntrada(tipoId, Tamanho.M, Sexo.MASCULINO, 10));

        verify(uniformeRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoEstornarEntradaQuandoEstoqueAtualEMenorQueAQuantidade() {
        UUID tipoId = UUID.randomUUID();
        Uniforme existente = Uniforme.builder().id(UUID.randomUUID()).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(2).build();

        when(uniformeRepository.findByTipoUniformeIdAndTamanhoAndSexo(tipoId, Tamanho.M, Sexo.MASCULINO))
                .thenReturn(Optional.of(existente));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> uniformeService.estornarEntrada(tipoId, Tamanho.M, Sexo.MASCULINO, 10));

        assertEquals("Não é possível estornar a entrada: o estoque ficaria negativo, pois parte das unidades já saiu em pedidos", exception.getMessage());
        verify(uniformeRepository, never()).save(any());
    }

    @Test
    void deveDarSaidaComSucessoQuandoHaEstoqueSuficiente() {
        UUID uniformeId = UUID.randomUUID();
        Uniforme uniforme = Uniforme.builder().id(uniformeId).quantidade(10).build();

        when(uniformeRepository.buscarComLockPorId(uniformeId)).thenReturn(Optional.of(uniforme));
        when(uniformeRepository.save(any(Uniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        uniformeService.darSaida(uniformeId, 6);

        ArgumentCaptor<Uniforme> captor = ArgumentCaptor.forClass(Uniforme.class);
        verify(uniformeRepository).save(captor.capture());
        assertEquals(4, captor.getValue().getQuantidade());
    }

    @Test
    void deveLancarExcecaoAoDarSaidaComQuantidadeMaiorQueEstoque() {
        UUID uniformeId = UUID.randomUUID();
        Uniforme uniforme = Uniforme.builder().id(uniformeId).quantidade(5).build();

        when(uniformeRepository.buscarComLockPorId(uniformeId)).thenReturn(Optional.of(uniforme));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> uniformeService.darSaida(uniformeId, 10));

        assertEquals("Quantidade solicitada (10) maior que o estoque disponível (5)", exception.getMessage());
        verify(uniformeRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUniformeNaoExisteAoDarSaida() {
        UUID uniformeId = UUID.randomUUID();
        when(uniformeRepository.buscarComLockPorId(uniformeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> uniformeService.darSaida(uniformeId, 5));
    }

    @Test
    void deveEstornarSaidaSomandoQuantidade() {
        UUID uniformeId = UUID.randomUUID();
        Uniforme uniforme = Uniforme.builder().id(uniformeId).quantidade(4).build();

        when(uniformeRepository.buscarComLockPorId(uniformeId)).thenReturn(Optional.of(uniforme));
        when(uniformeRepository.save(any(Uniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        uniformeService.estornarSaida(uniformeId, 6);

        ArgumentCaptor<Uniforme> captor = ArgumentCaptor.forClass(Uniforme.class);
        verify(uniformeRepository).save(captor.capture());
        assertEquals(10, captor.getValue().getQuantidade());
    }

    @Test
    void deveLancarExcecaoQuandoUniformeNaoExisteAoEstornarSaida() {
        UUID uniformeId = UUID.randomUUID();
        when(uniformeRepository.buscarComLockPorId(uniformeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> uniformeService.estornarSaida(uniformeId, 5));
        verify(uniformeRepository, never()).save(any());
    }

    @Test
    void deveBuscarTodosUniformesEntidades() {
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme1 = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(10).build();
        Uniforme uniforme2 = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipo).tamanho(Tamanho.G).sexo(Sexo.FEMININO).quantidade(5).build();

        when(uniformeRepository.findAll()).thenReturn(List.of(uniforme1, uniforme2));

        List<Uniforme> resultado = uniformeService.buscarTodosUniformesEntidades();

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(uniforme1));
        assertTrue(resultado.contains(uniforme2));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaUniformesCadastrados() {
        when(uniformeRepository.findAll()).thenReturn(List.of());

        List<Uniforme> resultado = uniformeService.buscarTodosUniformesEntidades();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveMarcarAlertaQuandoQuantidadeEstaAbaixoDoMinimo() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").estoqueBaixo(10).build();
        Uniforme uniforme = Uniforme.builder().id(id).tipoUniforme(tipo).tamanho(Tamanho.P).sexo(Sexo.FEMININO).quantidade(9).build();

        when(uniformeRepository.findById(id)).thenReturn(Optional.of(uniforme));

        ResponseUniformeDTO response = uniformeService.buscarUniforme(id);

        assertEquals(10, response.estoqueBaixo());
        assertTrue(response.estoqueBaixoAlerta());
    }

    @Test
    void naoDeveMarcarAlertaQuandoQuantidadeEstaAcimaDoMinimo() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").estoqueBaixo(10).build();
        Uniforme uniforme = Uniforme.builder().id(id).tipoUniforme(tipo).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(11).build();

        when(uniformeRepository.findById(id)).thenReturn(Optional.of(uniforme));

        assertFalse(uniformeService.buscarUniforme(id).estoqueBaixoAlerta());
    }
    @Test
    void deveMarcarAlertaQuandoQuantidadeEIgualAoMinimo() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").estoqueBaixo(10).build();
        Uniforme uniforme = Uniforme.builder().id(id).tipoUniforme(tipo).tamanho(Tamanho.P).sexo(Sexo.FEMININO).quantidade(10).build();

        when(uniformeRepository.findById(id)).thenReturn(Optional.of(uniforme));

        ResponseUniformeDTO response = uniformeService.buscarUniforme(id);

        assertEquals(10, response.estoqueBaixo());
        assertTrue(response.estoqueBaixoAlerta());
    }

    @Test
    void deveConsiderarOProprioLimiteComoEstoqueBaixo() {
        // CT07: 5 peças com limite 5 já é baixo; 6 não é; 0 é esgotado.
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").estoqueBaixo(5).build();

        assertTrue(Uniforme.builder().tipoUniforme(tipo).quantidade(5).build().estaComEstoqueBaixo());
        assertFalse(Uniforme.builder().tipoUniforme(tipo).quantidade(6).build().estaComEstoqueBaixo());

        assertEquals(SituacaoEstoque.BAIXO, SituacaoEstoque.calcular(5, 5));
        assertEquals(SituacaoEstoque.BAIXO, SituacaoEstoque.calcular(4, 5));
        assertEquals(SituacaoEstoque.OK, SituacaoEstoque.calcular(6, 5));
        assertEquals(SituacaoEstoque.ESGOTADO, SituacaoEstoque.calcular(0, 5));
        assertEquals(SituacaoEstoque.ESGOTADO, SituacaoEstoque.calcular(0, 0));
    }

    @Test
    void deveMontarOPainelAPartirDaView() {
        UUID uniformeId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        EstoqueView linha = mock(EstoqueView.class);
        when(linha.getUniformeId()).thenReturn(uniformeId);
        when(linha.getTipoUniformeId()).thenReturn(tipoId);
        when(linha.getTipo()).thenReturn("Camiseta");
        when(linha.getTamanho()).thenReturn("M");
        when(linha.getSexo()).thenReturn("FEMININO");
        when(linha.getQuantidade()).thenReturn(5);
        when(linha.getEstoqueBaixo()).thenReturn(5);
        when(linha.getSituacao()).thenReturn("BAIXO");
        when(uniformeRepository.buscarPainelEstoque()).thenReturn(List.of(linha));

        List<ResponseEstoquePainelDTO> painel = uniformeService.buscarPainelEstoque();

        assertEquals(List.of(new ResponseEstoquePainelDTO(uniformeId, tipoId, "Camiseta", Tamanho.M, Sexo.FEMININO, 5, 5, SituacaoEstoque.BAIXO)), painel);
    }

    @Test
    void deveTravarOEstoqueSempreNaMesmaOrdemSemRepetir() {
        UUID menor = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID maior = UUID.fromString("00000000-0000-0000-0000-000000000002");

        uniformeService.travarEstoque(List.of(maior, menor, maior));

        InOrder ordem = inOrder(uniformeRepository);
        ordem.verify(uniformeRepository).buscarComLockPorId(menor);
        ordem.verify(uniformeRepository).buscarComLockPorId(maior);
        verify(uniformeRepository, times(2)).buscarComLockPorId(any());
    }

    @Test
    void deveTravarAsCombinacoesSempreNaMesmaOrdem() {
        UUID tipo = UUID.fromString("00000000-0000-0000-0000-000000000001");
        CombinacaoUniforme g = new CombinacaoUniforme(tipo, Tamanho.G, Sexo.MASCULINO);
        CombinacaoUniforme p = new CombinacaoUniforme(tipo, Tamanho.P, Sexo.FEMININO);

        uniformeService.travarEstoquePorCombinacao(List.of(g, p, g));

        InOrder ordem = inOrder(uniformeRepository);
        ordem.verify(uniformeRepository).findByTipoUniformeIdAndTamanhoAndSexo(tipo, Tamanho.P, Sexo.FEMININO);
        ordem.verify(uniformeRepository).findByTipoUniformeIdAndTamanhoAndSexo(tipo, Tamanho.G, Sexo.MASCULINO);
        verify(uniformeRepository, times(2)).findByTipoUniformeIdAndTamanhoAndSexo(any(), any(), any());
    }
}
