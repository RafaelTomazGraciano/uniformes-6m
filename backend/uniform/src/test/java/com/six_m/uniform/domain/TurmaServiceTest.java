package com.six_m.uniform.domain;

import com.six_m.uniform.domain.aluno.AlunoRepository;
import com.six_m.uniform.domain.turma.Turma;
import com.six_m.uniform.domain.turma.TurmaRepository;
import com.six_m.uniform.domain.turma.TurmaService;
import com.six_m.uniform.domain.turma.dto.RequestAtualizarTurmaDTO;
import com.six_m.uniform.domain.turma.dto.RequestCriarTurmaDTO;
import com.six_m.uniform.domain.turma.dto.ResponseTurmaDTO;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import com.six_m.uniform.shared.enums.Ensino;
import com.six_m.uniform.shared.enums.Turno;
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
public class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private TurmaService turmaService;

    @Test
    void deveCriarTurmaComSucesso() {
        RequestCriarTurmaDTO dto = new RequestCriarTurmaDTO("Turma A", Turno.DIURNO, Ensino.FUNDAMENTAL);

        UUID idGerado = UUID.randomUUID();
        when(turmaRepository.save(any(Turma.class))).thenAnswer(invocation -> {
            Turma turmaSalva = invocation.getArgument(0);
            turmaSalva.setId(idGerado);
            return turmaSalva;
        });

        ResponseTurmaDTO response = turmaService.criarTurma(dto);

        assertEquals(idGerado, response.id());
        assertEquals("Turma A", response.nome());
        assertEquals(Turno.DIURNO, response.turno());
        assertEquals(Ensino.FUNDAMENTAL, response.ensino());
    }

    @Test
    void deveSalvarTurmaComOsCamposCorretosAoCriar() {
        RequestCriarTurmaDTO dto = new RequestCriarTurmaDTO("Turma B", Turno.NOTURNO, Ensino.TECNICO);

        when(turmaRepository.save(any(Turma.class))).thenAnswer(invocation -> invocation.getArgument(0));

        turmaService.criarTurma(dto);

        ArgumentCaptor<Turma> captor = ArgumentCaptor.forClass(Turma.class);
        verify(turmaRepository).save(captor.capture());

        assertEquals("Turma B", captor.getValue().getNome());
        assertEquals(Turno.NOTURNO, captor.getValue().getTurno());
        assertEquals(Ensino.TECNICO, captor.getValue().getEnsino());
    }

    @Test
    void deveBuscarTodasTurmasPaginado() {
        Turma turma1 = Turma.builder().id(UUID.randomUUID()).nome("Turma A").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        Turma turma2 = Turma.builder().id(UUID.randomUUID()).nome("Turma B").turno(Turno.VESPERTINO).ensino(Ensino.MEDIO).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(turmaRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(turma1, turma2), pageable, 2));

        var resultado = turmaService.buscarTodasTurmas(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals("Turma A", resultado.getContent().get(0).nome());
        assertEquals("Turma B", resultado.getContent().get(1).nome());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaTurmas() {
        Pageable pageable = PageRequest.of(0, 10);
        when(turmaRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = turmaService.buscarTodasTurmas(pageable);

        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.getTotalElements());
    }

    @Test
    void deveBuscarTurmaPorId() {
        UUID id = UUID.randomUUID();
        Turma turma = Turma.builder().id(id).nome("Turma Teste").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();

        when(turmaRepository.findById(id)).thenReturn(Optional.of(turma));

        ResponseTurmaDTO response = turmaService.buscarTurma(id);

        assertEquals(id, response.id());
        assertEquals("Turma Teste", response.nome());
        assertEquals(Turno.DIURNO, response.turno());
        assertEquals(Ensino.FUNDAMENTAL, response.ensino());
    }

    @Test
    void deveLancarExcecaoQuandoTurmaNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(turmaRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> turmaService.buscarTurma(id));

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void deveAtualizarTurmaComSucesso() {
        UUID id = UUID.randomUUID();
        Turma turmaExistente = Turma.builder().id(id).nome("Turma Antiga").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        RequestAtualizarTurmaDTO dto = new RequestAtualizarTurmaDTO("Turma Nova", Turno.NOTURNO, Ensino.MEDIO);

        when(turmaRepository.findById(id)).thenReturn(Optional.of(turmaExistente));
        when(turmaRepository.save(any(Turma.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseTurmaDTO response = turmaService.atualizarTurma(id, dto);

        assertEquals(id, response.id());
        assertEquals("Turma Nova", response.nome());
        assertEquals(Turno.NOTURNO, response.turno());
        assertEquals(Ensino.MEDIO, response.ensino());
    }

    @Test
    void deveLancarExcecaoQuandoTurmaNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        RequestAtualizarTurmaDTO dto = new RequestAtualizarTurmaDTO("Turma Nova", Turno.NOTURNO, Ensino.MEDIO);

        when(turmaRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> turmaService.atualizarTurma(id, dto));

        assertTrue(exception.getMessage().contains(id.toString()));
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveDeletarTurmaComSucessoQuandoNaoHaAlunosVinculados() {
        UUID id = UUID.randomUUID();
        Turma turma = Turma.builder().id(id).nome("Turma Teste").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();

        when(turmaRepository.findById(id)).thenReturn(Optional.of(turma));
        when(alunoRepository.existsByTurmaId(id)).thenReturn(false);

        MessageResponseDTO resultado = turmaService.deletarTurma(id);

        assertEquals("Turma deletada com sucesso", resultado.message());
        verify(turmaRepository).delete(turma);
    }

    @Test
    void deveLancarExcecaoAoDeletarTurmaComAlunosVinculados() {
        UUID id = UUID.randomUUID();
        Turma turma = Turma.builder().id(id).nome("Turma Teste").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();

        when(turmaRepository.findById(id)).thenReturn(Optional.of(turma));
        when(alunoRepository.existsByTurmaId(id)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> turmaService.deletarTurma(id));

        assertEquals("Não é possível excluir a turma: existem alunos vinculados a ela", exception.getMessage());
        verify(turmaRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoQuandoTurmaNaoExisteAoDeletar() {
        UUID id = UUID.randomUUID();
        when(turmaRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> turmaService.deletarTurma(id));

        assertTrue(exception.getMessage().contains(id.toString()));
        verify(alunoRepository, never()).existsByTurmaId(any());
        verify(turmaRepository, never()).delete(any());
    }
}