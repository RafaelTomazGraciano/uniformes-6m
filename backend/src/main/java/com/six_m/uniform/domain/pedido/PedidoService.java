package com.six_m.uniform.domain.pedido;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.aluno.AlunoRepository;
import com.six_m.uniform.domain.pedido.dto.RequestAtualizarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.RequestCriarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.RequestItemSaidaDTO;
import com.six_m.uniform.domain.pedido.dto.ResponsePedidoDTO;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeService;
import com.six_m.uniform.domain.pedidoUniforme.dto.ResponsePedidoUniformeDTO;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    static final String ALUNO_SEM_IDENTIFICACAO = "Aluno removido";
    static final String USUARIO_SEM_IDENTIFICACAO = "Usuário removido";
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PedidoRepository pedidoRepository;
    private final AlunoRepository alunoRepository;
    private final PedidoUniformeService pedidoUniformeService;
    private final UniformeService uniformeService;

    @Transactional
    public ResponsePedidoDTO criarPedido(RequestCriarPedidoDTO dto, Usuario usuario) {
        Aluno aluno = buscarAlunoOuFalhar(dto.alunoId());
        garantirAlunoAtivo(aluno);

        uniformeService.travarEstoque(idsDosUniformes(dto.itens()));

        /* Sem data, a entrega sumia dos relatórios, que filtram por período:
           quem não informa está registrando a entrega agora. */
        Pedido pedido = Pedido.builder()
                .aluno(aluno)
                .usuario(usuario)
                .dataEfetivada(dto.dataEfetivada() != null ? dto.dataEfetivada() : LocalDateTime.now())
                .build();

        pedido = pedidoRepository.save(pedido);

        List<PedidoUniforme> itens = pedidoUniformeService.criarItensParaPedido(pedido, dto.itens());

        for (PedidoUniforme item : itens) {
            uniformeService.darSaida(item.getUniforme().getId(), item.getQuantidade());
        }

        return toResponseDTO(pedido, itens);
    }

    @Transactional(readOnly = true)
    public Page<ResponsePedidoDTO> buscarTodosPedidos(UUID alunoId, Pageable pageable) {
        Page<Pedido> pagina = alunoId == null
                ? pedidoRepository.findAll(pageable)
                : pedidoRepository.findByAlunoId(alunoId, pageable);

        List<UUID> pedidoIds = pagina.getContent().stream().map(Pedido::getId).toList();
        Map<UUID, List<PedidoUniforme>> itensPorPedido = pedidoUniformeService.buscarItensPorPedidos(pedidoIds);

        return pagina.map(pedido -> toResponseDTO(pedido, itensPorPedido.getOrDefault(pedido.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public ResponsePedidoDTO buscarPedido(UUID id) {
        Pedido pedido = buscarPedidoOuFalhar(id);
        List<PedidoUniforme> itens = pedidoUniformeService.buscarItensPorPedido(id);
        return toResponseDTO(pedido, itens);
    }

    @Transactional
    public ResponsePedidoDTO atualizarPedido(UUID id, RequestAtualizarPedidoDTO dto, Usuario usuarioQueEditou) {
        Pedido pedido = buscarPedidoOuFalhar(id);
        Aluno aluno = buscarAlunoOuFalhar(dto.alunoId());
        /* Corrigir uma entrega antiga de quem já saiu é permitido; transferi-la
           para um aluno desligado seria uma entrega nova, e não é. */
        if (!Objects.equals(aluno.getId(), idDoAluno(pedido))) {
            garantirAlunoAtivo(aluno);
        }

        List<PedidoUniforme> itensAntigos = pedidoUniformeService.buscarItensPorPedido(id);

        List<UUID> uniformesEnvolvidos = new ArrayList<>(idsDosUniformes(dto.itens()));
        itensAntigos.forEach(item -> uniformesEnvolvidos.add(item.getUniforme().getId()));
        uniformeService.travarEstoque(uniformesEnvolvidos);

        for (PedidoUniforme itemAntigo : itensAntigos) {
            uniformeService.estornarSaida(itemAntigo.getUniforme().getId(), itemAntigo.getQuantidade());
        }
        pedidoUniformeService.deletarItensPorPedido(itensAntigos);

        List<PedidoUniforme> itensNovos = pedidoUniformeService.criarItensParaPedido(pedido, dto.itens());
        for (PedidoUniforme itemNovo : itensNovos) {
            uniformeService.darSaida(itemNovo.getUniforme().getId(), itemNovo.getQuantidade());
        }

        pedido.setAluno(aluno);
        // Editar sem informar a data mantém a original, em vez de apagá-la.
        if (dto.dataEfetivada() != null) {
            pedido.setDataEfetivada(dto.dataEfetivada());
        } else if (pedido.getDataEfetivada() == null) {
            pedido.setDataEfetivada(LocalDateTime.now());
        }
        pedido.setUsuario(usuarioQueEditou);
        pedido = pedidoRepository.save(pedido);

        return toResponseDTO(pedido, itensNovos);
    }

    private List<UUID> idsDosUniformes(List<RequestItemSaidaDTO> itens) {
        return itens.stream().map(RequestItemSaidaDTO::uniformeId).toList();
    }

    private Pedido buscarPedidoOuFalhar(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado com o ID: " + id));
    }

    private Aluno buscarAlunoOuFalhar(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aluno não encontrado com o ID: " + id));
    }

    private void garantirAlunoAtivo(Aluno aluno) {
        if (aluno.isAtivo()) {
            return;
        }
        String quando = aluno.getDataSaida() != null ? " em " + aluno.getDataSaida().format(DATA_BR) : "";
        throw new BadRequestException("Este aluno saiu da escola" + quando
                + " e não pode receber novas entregas. Reative o cadastro para registrar a entrega.");
    }

    private UUID idDoAluno(Pedido pedido) {
        return pedido.getAluno() != null ? pedido.getAluno().getId() : pedido.getAlunoId();
    }

    private ResponsePedidoDTO toResponseDTO(Pedido pedido, List<PedidoUniforme> itens) {
        Aluno aluno = pedido.getAluno();
        Usuario usuario = pedido.getUsuario();
        List<ResponsePedidoUniformeDTO> itensDto = itens.stream()
                .map(item -> new ResponsePedidoUniformeDTO(
                        item.getId(),
                        pedido.getId(),
                        item.getUniforme().getId(),
                        item.getUniforme().getTipoUniforme().getTipo(),
                        item.getUniforme().getTamanho(),
                        item.getUniforme().getSexo(),
                        item.getQuantidade()
                ))
                .toList();

        return new ResponsePedidoDTO(
                pedido.getId(),
                idDoAluno(pedido),
                aluno != null ? aluno.getNome() : ALUNO_SEM_IDENTIFICACAO,
                aluno != null && aluno.isAtivo(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNome() : USUARIO_SEM_IDENTIFICACAO,
                pedido.getDataEfetivada(),
                itensDto
        );
    }
}