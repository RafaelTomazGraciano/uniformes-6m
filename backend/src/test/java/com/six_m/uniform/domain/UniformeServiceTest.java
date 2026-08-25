package com.six_m.uniform.domain;

import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeRepository;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeRepository;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.domain.uniforme.dto.RequestAtualizarUniformeDTO;
import com.six_m.uniform.domain.uniforme.dto.RequestCriarUniformeDTO;
import com.six_m.uniform.domain.uniforme.dto.ResponseUniformeDTO;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
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
    private TipoUniformeRepository tipoUniformeRepository;

    @InjectMocks
    private UniformeService uniformeService;

    @Test
    void deveCriarUniformeComSucesso() {
        UUID tipoId = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(tipoId).tipo("Camiseta").build();
        RequestCriarUniformeDTO dto = new RequestCriarUniformeDTO(tipoId, Tamanho.M, 10, Sexo.MASCULINO);

        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.of(tipoUniforme));

        UUID idGerado = UUID.randomUUID();
        when(uniformeRepository.save(any(Uniforme.class))).thenAnswer(invocation -> {
            Uniforme salvo = invocation.getArgument(0);
            salvo.setId(idGerado);
            return salvo;
        });

        ResponseUniformeDTO response = uniformeService.criarUniforme(dto);

        assertEquals(idGerado, response.id());
        assertEquals(tipoId, response.tipoUniformeId());
        assertEquals("Camiseta", response.tipoUniformeNome());
        assertEquals(Tamanho.M, response.tamanho());
        assertEquals(10, response.quantidade());
        assertEquals(Sexo.MASCULINO, response.sexo());
    }

    @Test
    void deveLancarExcecaoQuandoTipoUniformeNaoExisteAoCriar() {
        UUID tipoId = UUID.randomUUID();
        RequestCriarUniformeDTO dto = new RequestCriarUniformeDTO(tipoId, Tamanho.M, 10, Sexo.MASCULINO);

        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> uniformeService.criarUniforme(dto));

        assertTrue(exception.getMessage().contains(tipoId.toString()));
        verify(uniformeRepository, never()).save(any());
    }

    @Test
    void deveSalvarUniformeComOsCamposCorretosAoCriar() {
        UUID tipoId = UUID.randomUUID();
        TipoUniforme tipoUniforme = TipoUniforme.builder().id(tipoId).tipo("Calça").build();
        RequestCriarUniformeDTO dto = new RequestCriarUniformeDTO(tipoId, Tamanho.GG, 5, Sexo.FEMININO);

        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.of(tipoUniforme));
        when(uniformeRepository.save(any(Uniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        uniformeService.criarUniforme(dto);

        ArgumentCaptor<Uniforme> captor = ArgumentCaptor.forClass(Uniforme.class);
        verify(uniformeRepository).save(captor.capture());

        assertEquals(tipoUniforme, captor.getValue().getTipoUniforme());
        assertEquals(Tamanho.GG, captor.getValue().getTamanho());
        assertEquals(5, captor.getValue().getQuantidade());
        assertEquals(Sexo.FEMININO, captor.getValue().getSexo());
    }

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
    void deveAtualizarUniformeComSucesso() {
        UUID id = UUID.randomUUID();
        UUID novoTipoId = UUID.randomUUID();

        TipoUniforme tipoAntigo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        TipoUniforme tipoNovo = TipoUniforme.builder().id(novoTipoId).tipo("Calça").build();
        Uniforme uniformeExistente = Uniforme.builder().id(id).tipoUniforme(tipoAntigo).tamanho(Tamanho.P).quantidade(5).sexo(Sexo.MASCULINO).build();

        RequestAtualizarUniformeDTO dto = new RequestAtualizarUniformeDTO(novoTipoId, Tamanho.GG, 20, Sexo.FEMININO);

        when(uniformeRepository.findById(id)).thenReturn(Optional.of(uniformeExistente));
        when(tipoUniformeRepository.findById(novoTipoId)).thenReturn(Optional.of(tipoNovo));
        when(uniformeRepository.save(any(Uniforme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseUniformeDTO response = uniformeService.atualizarUniforme(id, dto);

        assertEquals(novoTipoId, response.tipoUniformeId());
        assertEquals("Calça", response.tipoUniformeNome());
        assertEquals(Tamanho.GG, response.tamanho());
        assertEquals(20, response.quantidade());
        assertEquals(Sexo.FEMININO, response.sexo());
    }

    @Test
    void deveLancarExcecaoQuandoUniformeNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        RequestAtualizarUniformeDTO dto = new RequestAtualizarUniformeDTO(UUID.randomUUID(), Tamanho.M, 10, Sexo.MASCULINO);

        when(uniformeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> uniformeService.atualizarUniforme(id, dto));
        verify(tipoUniformeRepository, never()).findById(any());
        verify(uniformeRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNovoTipoUniformeNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();

        TipoUniforme tipoAntigo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniformeExistente = Uniforme.builder().id(id).tipoUniforme(tipoAntigo).tamanho(Tamanho.M).quantidade(10).sexo(Sexo.MASCULINO).build();
        RequestAtualizarUniformeDTO dto = new RequestAtualizarUniformeDTO(tipoId, Tamanho.M, 10, Sexo.MASCULINO);

        when(uniformeRepository.findById(id)).thenReturn(Optional.of(uniformeExistente));
        when(tipoUniformeRepository.findById(tipoId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> uniformeService.atualizarUniforme(id, dto));

        assertTrue(exception.getMessage().contains(tipoId.toString()));
        verify(uniformeRepository, never()).save(any());
    }

    @Test
    void deveDeletarUniformeComSucesso() {
        UUID id = UUID.randomUUID();
        TipoUniforme tipo = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        Uniforme uniforme = Uniforme.builder().id(id).tipoUniforme(tipo).tamanho(Tamanho.M).quantidade(10).sexo(Sexo.MASCULINO).build();

        when(uniformeRepository.findById(id)).thenReturn(Optional.of(uniforme));

        MessageResponseDTO resultado = uniformeService.deletarUniforme(id);

        assertEquals("Uniforme deletado com sucesso", resultado.message());
        verify(uniformeRepository).delete(uniforme);
    }

    @Test
    void deveLancarExcecaoQuandoUniformeNaoExisteAoDeletar() {
        UUID id = UUID.randomUUID();
        when(uniformeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> uniformeService.deletarUniforme(id));
        verify(uniformeRepository, never()).delete(any());
    }
}