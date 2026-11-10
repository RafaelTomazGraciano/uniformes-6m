package com.six_m.uniform.domain.requisicao;

import com.six_m.uniform.domain.requisicao.dto.RequestCriarRequisicaoDTO;
import com.six_m.uniform.domain.requisicao.dto.ResponseRequisicaoDTO;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeService;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.enums.StatusRequisicao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequisicaoService {

    private final RequisicaoRepository requisicaoRepository;
    private final TipoUniformeService tipoUniformeService;

    @Transactional
    public ResponseRequisicaoDTO criarRequisicao(RequestCriarRequisicaoDTO dto, Usuario usuario) {
        TipoUniforme tipoUniforme = tipoUniformeService.buscarTipoUniformeEntidade(dto.tipoUniformeId());

        Requisicao requisicao = Requisicao.builder()
                .tipoUniforme(tipoUniforme)
                .tamanho(dto.tamanho())
                .sexo(dto.sexo())
                .quantidade(dto.quantidade())
                .observacao(normalizarObservacao(dto.observacao()))
                .status(StatusRequisicao.PENDENTE)
                .usuario(usuario)
                .dataCriacao(LocalDateTime.now())
                .build();

        requisicao = requisicaoRepository.save(requisicao);

        return toResponseDTO(requisicao);
    }

    @Transactional(readOnly = true)
    public Page<ResponseRequisicaoDTO> buscarTodasRequisicoes(List<StatusRequisicao> status, Pageable pageable) {
        Page<Requisicao> pagina = status == null || status.isEmpty()
                ? requisicaoRepository.findAll(pageable)
                : requisicaoRepository.findByStatusIn(status, pageable);

        return pagina.map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseRequisicaoDTO buscarRequisicao(UUID id) {
        return toResponseDTO(buscarRequisicaoOuFalhar(id));
    }

    /**
     * Marca a requisição como atendida. Não mexe no estoque: as peças só entram
     * quando o lote do fornecedor é registrado.
     */
    @Transactional
    public ResponseRequisicaoDTO atenderRequisicao(UUID id) {
        return concluirRequisicao(id, StatusRequisicao.ATENDIDA, "atender");
    }

    @Transactional
    public ResponseRequisicaoDTO cancelarRequisicao(UUID id) {
        return concluirRequisicao(id, StatusRequisicao.CANCELADA, "cancelar");
    }

    private ResponseRequisicaoDTO concluirRequisicao(UUID id, StatusRequisicao novoStatus, String acao) {
        Requisicao requisicao = buscarRequisicaoOuFalhar(id);

        if (requisicao.getStatus() != StatusRequisicao.PENDENTE) {
            throw new BadRequestException("Só é possível " + acao + " uma requisição pendente. Esta requisição já está "
                    + requisicao.getStatus().getValor().toLowerCase());
        }

        requisicao.setStatus(novoStatus);
        requisicao.setDataConclusao(LocalDateTime.now());
        requisicao = requisicaoRepository.save(requisicao);

        return toResponseDTO(requisicao);
    }

    private Requisicao buscarRequisicaoOuFalhar(UUID id) {
        return requisicaoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Requisição não encontrada com o ID: " + id));
    }

    private String normalizarObservacao(String observacao) {
        if (observacao == null || observacao.isBlank()) {
            return null;
        }
        return observacao.trim();
    }

    private ResponseRequisicaoDTO toResponseDTO(Requisicao requisicao) {
        return new ResponseRequisicaoDTO(
                requisicao.getId(),
                requisicao.getTipoUniforme().getId(),
                requisicao.getTipoUniforme().getTipo(),
                requisicao.getTamanho(),
                requisicao.getSexo(),
                requisicao.getQuantidade(),
                requisicao.getObservacao(),
                requisicao.getStatus(),
                requisicao.getUsuario().getId(),
                requisicao.getUsuario().getNome(),
                requisicao.getDataCriacao(),
                requisicao.getDataConclusao()
        );
    }
}
