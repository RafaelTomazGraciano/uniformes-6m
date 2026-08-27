package com.six_m.uniform.domain.pedido;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.aluno.AlunoRepository;
import com.six_m.uniform.domain.pedido.dto.RequestAtualizarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.RequestCriarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.ResponsePedidoDTO;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniformeRepository;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final AlunoRepository alunoRepository;

    @Transactional
    public ResponsePedidoDTO criarPedido(RequestCriarPedidoDTO dto, Usuario usuario) {
        Aluno aluno = buscarAlunoOuFalhar(dto.alunoId());

        Pedido pedido = Pedido.builder()
                .aluno(aluno)
                .usuario(usuario)
                .dataEfetivada(dto.dataEfetivada())
                .build();

        pedido = pedidoRepository.save(pedido);

        return toResponseDTO(pedido);
    }

    @Transactional(readOnly = true)
    public Page<ResponsePedidoDTO> buscarTodosPedidos(Pageable pageable) {
        return pedidoRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponsePedidoDTO buscarPedido(UUID id) {
        return toResponseDTO(buscarPedidoOuFalhar(id));
    }

    @Transactional
    public ResponsePedidoDTO atualizarPedido(UUID id, RequestAtualizarPedidoDTO dto) {
        Pedido pedido = buscarPedidoOuFalhar(id);
        Aluno aluno = buscarAlunoOuFalhar(dto.alunoId());

        pedido.setAluno(aluno);
        pedido.setDataEfetivada(dto.dataEfetivada());

        pedido = pedidoRepository.save(pedido);

        return toResponseDTO(pedido);
    }

    private Pedido buscarPedidoOuFalhar(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado com o ID: " + id));
    }

    private Aluno buscarAlunoOuFalhar(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aluno não encontrado com o ID: " + id));
    }

    private ResponsePedidoDTO toResponseDTO(Pedido pedido) {
        return new ResponsePedidoDTO(
                pedido.getId(),
                pedido.getAluno().getId(),
                pedido.getAluno().getNome(),
                pedido.getUsuario().getId(),
                pedido.getUsuario().getNome(),
                pedido.getDataEfetivada()
        );
    }
}