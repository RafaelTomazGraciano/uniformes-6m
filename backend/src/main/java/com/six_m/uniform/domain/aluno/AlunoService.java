package com.six_m.uniform.domain.aluno;

import com.six_m.uniform.domain.aluno.dto.RequestAtualizarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.RequestCriarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.ResponseAlunoDTO;
import com.six_m.uniform.domain.turma.Turma;
import com.six_m.uniform.domain.turma.TurmaRepository;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;

    @Transactional
    public ResponseAlunoDTO criarAluno(RequestCriarAlunoDTO dto) {
        Turma turma = buscarTurmaOuFalhar(dto.turmaId());

        Aluno aluno = Aluno.builder()
                .nome(dto.nome())
                .turma(turma)
                .build();

        aluno = alunoRepository.save(aluno);

        return toResponseDTO(aluno);
    }

    @Transactional(readOnly = true)
    public Page<ResponseAlunoDTO> buscarTodosAlunos(Pageable pageable) {
        return alunoRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseAlunoDTO buscarAluno(UUID id) {
        return toResponseDTO(buscarAlunoOuFalhar(id));
    }

    @Transactional
    public ResponseAlunoDTO atualizarAluno(UUID id, RequestAtualizarAlunoDTO dto) {
        Aluno aluno = buscarAlunoOuFalhar(id);
        Turma turma = buscarTurmaOuFalhar(dto.turmaId());

        aluno.setNome(dto.nome());
        aluno.setTurma(turma);

        aluno = alunoRepository.save(aluno);

        return toResponseDTO(aluno);
    }

    @Transactional
    public MessageResponseDTO deletarAluno(UUID id) {
        Aluno aluno = buscarAlunoOuFalhar(id);

        alunoRepository.delete(aluno);

        return new MessageResponseDTO("Aluno deletado com sucesso");
    }

    private Aluno buscarAlunoOuFalhar(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aluno não encontrado com o ID: " + id));
    }

    private Turma buscarTurmaOuFalhar(UUID turmaId) {
        return turmaRepository.findById(turmaId)
                .orElseThrow(() -> new NotFoundException("Turma não encontrada com o ID: " + turmaId));
    }

    private ResponseAlunoDTO toResponseDTO(Aluno aluno) {
        return new ResponseAlunoDTO(aluno.getId(), aluno.getNome(), aluno.getTurma().getId(), aluno.getTurma().getNome());
    }
}