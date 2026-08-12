package com.six_m.uniform.domain;

import com.six_m.uniform.domain.escola.Escola;
import com.six_m.uniform.domain.escola.EscolaRepository;
import com.six_m.uniform.domain.escola.EscolaService;
import com.six_m.uniform.domain.escola.dto.ResponseEscolaDTO;
import com.six_m.uniform.enums.TipoEscola;
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
public class EscolaServiceTest {

    @Mock
    private EscolaRepository escolaRepository;

    @InjectMocks
    private EscolaService escolaService;

    @Test
    void deveBuscarEscolaPorId() {
        UUID id = UUID.randomUUID();
        Escola escola = Escola.builder()
                .id(id)
                .nome("Escola Teste")
                .tipo(TipoEscola.PUBLICA)
                .build();

        when(escolaRepository.findById(id)).thenReturn(Optional.of(escola));

        ResponseEscolaDTO response = escolaService.buscarEscola(id);

        assertEquals(id, response.id());
        assertEquals("Escola Teste", response.nome());
        assertEquals(TipoEscola.PUBLICA, response.tipo());
    }

    @Test
    void deveLancarExcecaoQuandoEscolaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(escolaRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> escolaService.buscarEscola(id));

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void deveBuscarTodasEscolasPaginado() {
        Escola escola1 = Escola.builder().id(UUID.randomUUID()).nome("Escola A").tipo(TipoEscola.PUBLICA).build();
        Escola escola2 = Escola.builder().id(UUID.randomUUID()).nome("Escola B").tipo(TipoEscola.PARCEIRA).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(escolaRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(escola1, escola2), pageable, 2));

        var resultado = escolaService.buscarTodasEscolas(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals("Escola A", resultado.getContent().get(0).nome());
        assertEquals("Escola B", resultado.getContent().get(1).nome());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaEscolas() {
        Pageable pageable = PageRequest.of(0, 10);
        when(escolaRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = escolaService.buscarTodasEscolas(pageable);

        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.getTotalElements());
    }
}