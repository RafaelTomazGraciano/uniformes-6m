package com.six_m.uniform.domain;

import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeService;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeRepository;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.domain.uniforme.dto.ResponseUniformeDTO;
import com.six_m.uniform.exception.NotFoundException;
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
public class UniformeServiceTest {

    @Mock
    private UniformeRepository uniformeRepository;

    @Mock
    private TipoUniformeService tipoUniformeService;

    @InjectMocks
    private UniformeService uniformeService;

    @Test
    void deveBuscarTodosUniformesPaginado() {
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme1 = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipo).tamanho(Tamanho.P).quantidade(3).sexo(Sexo.MASCULINO).build();
        Uniforme uniforme2 = Uniforme.builder().id(UUID.randomUUID()).tipoUniforme(tipo).tamanho(Tamanho.G).quantidade(7).sexo(Sexo.FEMININO).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(uniformeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(uniforme1, uniforme2), pageable, 2));

        var resultado = uniformeService.buscarTodosUniformes(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals(Tamanho.P, resultado.getContent().get(0).tamanho());
        assertEquals(Tamanho.G, resultado.getContent().get(1).tamanho());
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
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().id(id).tipoUniforme(tipo).tamanho(Tamanho.M).quantidade(10).sexo(Sexo.MASCULINO).build();

        when(uniformeRepository.findById(id)).thenReturn(Optional.of(uniforme));

        ResponseUniformeDTO response = uniformeService.buscarUniforme(id);

        assertEquals(id, response.id());
        assertEquals(Tamanho.M, response.tamanho());
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
}