package com.six_m.uniform.domain.pedido;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.aluno.AlunoRepository;
import com.six_m.uniform.domain.pedido.dto.RequestAtualizarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.RequestCriarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.ResponsePedidoDTO;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeService;
import com.six_m.uniform.domain.pedidoUniforme.dto.ResponsePedidoUniformeDTO;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final AlunoRepository alunoRepository;
    private final PedidoUniformeService pedidoUniformeService;
    private final UniformeService uniformeService;

    @Transactional
    public ResponsePedidoDTO criarPedido(RequestCriarPedidoDTO dto, Usuario usuario) {
        Aluno aluno = buscarAlunoOuFalhar(dto.alunoId());

        Pedido pedido = Pedido.builder()
                .aluno(aluno)
                .usuario(usuario)
                .dataEfetivada(dto.dataEfetivada())
                .build();

        pedido = pedidoRepository.save(pedido);

        List<PedidoUniforme> itens = pedidoUniformeService.criarItensParaPedido(pedido, dto.itens());

        for (PedidoUniforme item : itens) {
            uniformeService.darSaida(item.getUniforme().getId(), item.getQuantidade());
        }

        return toResponseDTO(pedido, itens);
    }

    @Transactional(readOnly = true)
    public Page<ResponsePedidoDTO> buscarTodosPedidos(Pageable pageable) {
        Page<Pedido> pagina = pedidoRepository.findAll(pageable);

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
    public ResponsePedidoDTO atualizarPedido(UUID id, RequestAtualizarPedidoDTO dto) {
        Pedido pedido = buscarPedidoOuFalhar(id);
        Aluno aluno = buscarAlunoOuFalhar(dto.alunoId());

        List<PedidoUniforme> itensAntigos = pedidoUniformeService.buscarItensPorPedido(id);
        for (PedidoUniforme itemAntigo : itensAntigos) {
            uniformeService.estornarSaida(itemAntigo.getUniforme().getId(), itemAntigo.getQuantidade());
        }
        pedidoUniformeService.deletarItensPorPedido(itensAntigos);

        List<PedidoUniforme> itensNovos = pedidoUniformeService.criarItensParaPedido(pedido, dto.itens());
        for (PedidoUniforme itemNovo : itensNovos) {
            uniformeService.darSaida(itemNovo.getUniforme().getId(), itemNovo.getQuantidade());
        }

        pedido.setAluno(aluno);
        pedido.setDataEfetivada(dto.dataEfetivada());
        pedido = pedidoRepository.save(pedido);

        return toResponseDTO(pedido, itensNovos);
    }

    private Pedido buscarPedidoOuFalhar(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado com o ID: " + id));
    }

    private Aluno buscarAlunoOuFalhar(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aluno não encontrado com o ID: " + id));
    }

    private ResponsePedidoDTO toResponseDTO(Pedido pedido, List<PedidoUniforme> itens) {
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
                pedido.getAluno().getId(),
                pedido.getAluno().getNome(),
                pedido.getUsuario().getId(),
                pedido.getUsuario().getNome(),
                pedido.getDataEfetivada(),
                itensDto
        );
    }
}