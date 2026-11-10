package com.six_m.uniform.domain.aluno;

import com.six_m.uniform.domain.aluno.dto.RequestAtualizarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.RequestCriarAlunoDTO;
import com.six_m.uniform.domain.aluno.dto.ResponseAlunoDTO;
import com.six_m.uniform.domain.pedido.PedidoRepository;
import com.six_m.uniform.domain.turma.Turma;
import com.six_m.uniform.domain.turma.TurmaRepository;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlunoService {

    static final String MENSAGEM_ALUNO_COM_ENTREGAS =
            "Este aluno tem entregas registradas. Use 'Registrar saída' para manter o histórico.";

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public ResponseAlunoDTO criarAluno(RequestCriarAlunoDTO dto) {
        Turma turma = buscarTurmaOuFalhar(dto.turmaId());

        Aluno aluno = Aluno.builder()
                .nome(dto.nome())
                .nomeResponsavel(dto.nomeResponsavel())
                .telefoneResponsavel(dto.telefoneResponsavel())
                .turma(turma)
                .build();

        aluno = alunoRepository.save(aluno);

        return toResponseDTO(aluno);
    }

    @Transactional(readOnly = true)
    public Page<ResponseAlunoDTO> buscarTodosAlunos(String nome, UUID turmaId, Boolean ativo, Pageable pageable) {
        Specification<Aluno> filtro = Specification.allOf(
                AlunoSpecifications.buscaPorNomeOuTurma(nome),
                AlunoSpecifications.daTurma(turmaId),
                AlunoSpecifications.comSituacao(ativo)
        );

        return alunoRepository.findAll(filtro, pageable)
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
        aluno.setNomeResponsavel(dto.nomeResponsavel());
        aluno.setTelefoneResponsavel(dto.telefoneResponsavel());
        aluno.setTurma(turma);

        aluno = alunoRepository.save(aluno);

        return toResponseDTO(aluno);
    }

    /* Desligamento: o aluno deixa de receber entregas, mas o cadastro (e, com ele,
       o histórico de uniformes recebidos) continua disponível. */
    @Transactional
    public ResponseAlunoDTO registrarSaida(UUID id) {
        Aluno aluno = buscarAlunoOuFalhar(id);

        if (!aluno.isAtivo()) {
            throw new BadRequestException("A saída deste aluno já foi registrada");
        }

        aluno.setAtivo(false);
        aluno.setDataSaida(LocalDateTime.now());
        aluno = alunoRepository.save(aluno);

        return toResponseDTO(aluno);
    }

    @Transactional
    public ResponseAlunoDTO reativarAluno(UUID id) {
        Aluno aluno = buscarAlunoOuFalhar(id);

        if (aluno.isAtivo()) {
            throw new BadRequestException("Este aluno já está ativo");
        }

        aluno.setAtivo(true);
        aluno.setDataSaida(null);
        aluno = alunoRepository.save(aluno);

        return toResponseDTO(aluno);
    }

    /* Excluir só serve para cadastro feito por engano: se o aluno já recebeu
       uniforme, apagar o cadastro esconderia (e quebraria) o histórico. */
    @Transactional
    public MessageResponseDTO deletarAluno(UUID id) {
        Aluno aluno = buscarAlunoOuFalhar(id);

        if (pedidoRepository.existsByAlunoId(id)) {
            throw new BadRequestException(MENSAGEM_ALUNO_COM_ENTREGAS);
        }

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
        return new ResponseAlunoDTO(aluno.getId(), aluno.getNome(), aluno.getNomeResponsavel(),
                aluno.getTelefoneResponsavel(), aluno.getTurma().getId(), aluno.getTurma().getNome(),
                aluno.isAtivo(), aluno.getDataSaida());
    }
}
