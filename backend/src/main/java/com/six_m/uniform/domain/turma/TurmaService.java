package com.six_m.uniform.domain.turma;

import com.six_m.uniform.domain.aluno.AlunoRepository;
import com.six_m.uniform.domain.turma.dto.RequestAtualizarTurmaDTO;
import com.six_m.uniform.domain.turma.dto.RequestCriarTurmaDTO;
import com.six_m.uniform.domain.turma.dto.ResponseTurmaDTO;
import com.six_m.uniform.exception.BadRequestException;
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
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;

    @Transactional
    public ResponseTurmaDTO criarTurma(RequestCriarTurmaDTO dto) {
        Turma turma = Turma.builder()
                .nome(dto.nome())
                .turno(dto.turno())
                .ensino(dto.ensino())
                .build();

        turma = turmaRepository.save(turma);

        return toResponseDTO(turma);
    }

    @Transactional(readOnly = true)
    public Page<ResponseTurmaDTO> buscarTodasTurmas(Pageable pageable) {
        return turmaRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseTurmaDTO buscarTurma(UUID id) {
        return toResponseDTO(buscarTurmaOuFalhar(id));
    }

    @Transactional
    public ResponseTurmaDTO atualizarTurma(UUID id, RequestAtualizarTurmaDTO dto) {
        Turma turma = buscarTurmaOuFalhar(id);

        turma.setNome(dto.nome());
        turma.setTurno(dto.turno());
        turma.setEnsino(dto.ensino());

        turma = turmaRepository.save(turma);

        return toResponseDTO(turma);
    }

    @Transactional
    public MessageResponseDTO deletarTurma(UUID id) {
        Turma turma = buscarTurmaOuFalhar(id);

        if (alunoRepository.existsByTurmaId(id)) {
            throw new BadRequestException("Não é possível excluir a turma: existem alunos vinculados a ela");
        }

        turmaRepository.delete(turma);

        return new MessageResponseDTO("Turma deletada com sucesso");
    }

    private Turma buscarTurmaOuFalhar(UUID id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Turma não encontrada com o ID: " + id));
    }

    private ResponseTurmaDTO toResponseDTO(Turma turma) {
        return new ResponseTurmaDTO(turma.getId(), turma.getNome(), turma.getTurno(), turma.getEnsino());
    }

}
