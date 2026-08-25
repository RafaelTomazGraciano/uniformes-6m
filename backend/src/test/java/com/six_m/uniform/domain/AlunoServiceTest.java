package com.six_m.uniform.domain;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.aluno.AlunoRepository;
import com.six_m.uniform.domain.aluno.AlunoService;
import com.six_m.uniform.domain.aluno.dto.RequestAtualizarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.RequestCriarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.ResponseAlunoDTO;
import com.six_m.uniform.domain.turma.Turma;
import com.six_m.uniform.domain.turma.TurmaRepository;
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
public class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @InjectMocks
    private AlunoService alunoService;

    @Test
    void deveCriarAlunoComSucesso() {
        UUID turmaId = UUID.randomUUID();
        Turma turma = Turma.builder().id(turmaId).nome("Turma A").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        RequestCriarAlunoDTO dto = new RequestCriarAlunoDTO("João", turmaId);

        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));

        UUID idGerado = UUID.randomUUID();
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno alunoSalvo = invocation.getArgument(0);
            alunoSalvo.setId(idGerado);
            return alunoSalvo;
        });

        ResponseAlunoDTO response = alunoService.criarAluno(dto);

        assertEquals(idGerado, response.id());
        assertEquals("João", response.nome());
        assertEquals(turmaId, response.turmaId());
        assertEquals("Turma A", response.turmaNome());
    }

    @Test
    void deveSalvarAlunoComOsCamposCorretosAoCriar() {
        UUID turmaId = UUID.randomUUID();
        Turma turma = Turma.builder().id(turmaId).nome("Turma A").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        RequestCriarAlunoDTO dto = new RequestCriarAlunoDTO("Maria", turmaId);

        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alunoService.criarAluno(dto);

        ArgumentCaptor<Aluno> captor = ArgumentCaptor.forClass(Aluno.class);
        verify(alunoRepository).save(captor.capture());

        assertEquals("Maria", captor.getValue().getNome());
        assertEquals(turma, captor.getValue().getTurma());
    }

    @Test
    void deveLancarExcecaoQuandoTurmaNaoExisteAoCriarAluno() {
        UUID turmaId = UUID.randomUUID();
        RequestCriarAlunoDTO dto = new RequestCriarAlunoDTO("João", turmaId);

        when(turmaRepository.findById(turmaId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> alunoService.criarAluno(dto));

        assertTrue(exception.getMessage().contains(turmaId.toString()));
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveBuscarTodosAlunosPaginado() {
        Turma turma = Turma.builder().id(UUID.randomUUID()).nome("Turma A").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        Aluno aluno1 = Aluno.builder().id(UUID.randomUUID()).nome("João").turma(turma).build();
        Aluno aluno2 = Aluno.builder().id(UUID.randomUUID()).nome("Maria").turma(turma).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(alunoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(aluno1, aluno2), pageable, 2));

        var resultado = alunoService.buscarTodosAlunos(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals("João", resultado.getContent().get(0).nome());
        assertEquals("Maria", resultado.getContent().get(1).nome());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaAlunos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(alunoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = alunoService.buscarTodosAlunos(pageable);

        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.getTotalElements());
    }

    @Test
    void deveBuscarAlunoPorId() {
        UUID id = UUID.randomUUID();
        Turma turma = Turma.builder().id(UUID.randomUUID()).nome("Turma A").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        Aluno aluno = Aluno.builder().id(id).nome("João").turma(turma).build();

        when(alunoRepository.findById(id)).thenReturn(Optional.of(aluno));

        ResponseAlunoDTO response = alunoService.buscarAluno(id);

        assertEquals(id, response.id());
        assertEquals("João", response.nome());
        assertEquals(turma.getId(), response.turmaId());
        assertEquals("Turma A", response.turmaNome());
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(alunoRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> alunoService.buscarAluno(id));

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void deveAtualizarAlunoComSucesso() {
        UUID id = UUID.randomUUID();
        UUID novaTurmaId = UUID.randomUUID();

        Turma turmaAntiga = Turma.builder().id(UUID.randomUUID()).nome("Turma A").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        Turma turmaNova = Turma.builder().id(novaTurmaId).nome("Turma B").turno(Turno.NOTURNO).ensino(Ensino.MEDIO).build();
        Aluno alunoExistente = Aluno.builder().id(id).nome("João").turma(turmaAntiga).build();

        RequestAtualizarAlunoDTO dto = new RequestAtualizarAlunoDTO("João Atualizado", novaTurmaId);

        when(alunoRepository.findById(id)).thenReturn(Optional.of(alunoExistente));
        when(turmaRepository.findById(novaTurmaId)).thenReturn(Optional.of(turmaNova));
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseAlunoDTO response = alunoService.atualizarAluno(id, dto);

        assertEquals(id, response.id());
        assertEquals("João Atualizado", response.nome());
        assertEquals(novaTurmaId, response.turmaId());
        assertEquals("Turma B", response.turmaNome());
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        RequestAtualizarAlunoDTO dto = new RequestAtualizarAlunoDTO("João", UUID.randomUUID());

        when(alunoRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> alunoService.atualizarAluno(id, dto));

        assertTrue(exception.getMessage().contains(id.toString()));
        verify(turmaRepository, never()).findById(any());
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNovaTurmaNaoExisteAoAtualizar() {
        UUID id = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        Turma turmaAntiga = Turma.builder().id(UUID.randomUUID()).nome("Turma A").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        Aluno alunoExistente = Aluno.builder().id(id).nome("João").turma(turmaAntiga).build();
        RequestAtualizarAlunoDTO dto = new RequestAtualizarAlunoDTO("João", turmaId);

        when(alunoRepository.findById(id)).thenReturn(Optional.of(alunoExistente));
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> alunoService.atualizarAluno(id, dto));

        assertTrue(exception.getMessage().contains(turmaId.toString()));
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveDeletarAlunoComSucesso() {
        UUID id = UUID.randomUUID();
        Turma turma = Turma.builder().id(UUID.randomUUID()).nome("Turma A").turno(Turno.DIURNO).ensino(Ensino.FUNDAMENTAL).build();
        Aluno aluno = Aluno.builder().id(id).nome("João").turma(turma).build();

        when(alunoRepository.findById(id)).thenReturn(Optional.of(aluno));

        MessageResponseDTO resultado = alunoService.deletarAluno(id);

        assertEquals("Aluno deletado com sucesso", resultado.message());
        verify(alunoRepository).delete(aluno);
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExisteAoDeletar() {
        UUID id = UUID.randomUUID();
        when(alunoRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> alunoService.deletarAluno(id));

        assertTrue(exception.getMessage().contains(id.toString()));
        verify(alunoRepository, never()).delete(any());
    }
}