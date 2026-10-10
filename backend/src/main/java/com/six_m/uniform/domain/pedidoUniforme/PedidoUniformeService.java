package com.six_m.uniform.domain.pedidoUniforme;


import com.six_m.uniform.domain.pedido.Pedido;
import com.six_m.uniform.domain.pedido.dto.RequestItemSaidaDTO;
import com.six_m.uniform.domain.pedidoUniforme.dto.ResponsePedidoUniformeDTO;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoUniformeService {

    private final PedidoUniformeRepository pedidoUniformeRepository;
    private final UniformeService uniformeService;

    @Transactional(readOnly = true)
    public Page<ResponsePedidoUniformeDTO> buscarTodosPedidosUniforme(Pageable pageable) {
        return pedidoUniformeRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponsePedidoUniformeDTO buscarPedidoUniforme(UUID id) {
        return toResponseDTO(buscarPedidoUniformeOuFalhar(id));
    }

    @Transactional
    public List<PedidoUniforme> criarItensParaPedido(Pedido pedido, List<RequestItemSaidaDTO> itensDto) {
        Map<UUID, Integer> quantidadePorUniforme = consolidarItens(itensDto);

        List<PedidoUniforme> itens = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entrada : quantidadePorUniforme.entrySet()) {
            Uniforme uniforme = uniformeService.buscarUniformeEntidade(entrada.getKey());

            PedidoUniforme pedidoUniforme = PedidoUniforme.builder()
                    .pedido(pedido)
                    .uniforme(uniforme)
                    .quantidade(entrada.getValue())
                    .build();

            itens.add(pedidoUniformeRepository.save(pedidoUniforme));
        }

        return itens;
    }

    @Transactional(readOnly = true)
    public List<PedidoUniforme> buscarItensPorPedido(UUID pedidoId) {
        return pedidoUniformeRepository.findByPedidoId(pedidoId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<PedidoUniforme>> buscarItensPorPedidos(Collection<UUID> pedidoIds) {
        return pedidoUniformeRepository.findByPedidoIdIn(pedidoIds).stream()
                .collect(Collectors.groupingBy(item -> item.getPedido().getId()));
    }

    @Transactional
    public void deletarItensPorPedido(List<PedidoUniforme> itens) {
        pedidoUniformeRepository.deleteAll(itens);
    }

    @Transactional(readOnly = true)
    public List<PedidoUniforme> buscarItensPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pedidoUniformeRepository.findByPedidoDataEfetivadaBetween(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<PedidoUniforme> buscarItensPorPeriodoETurma(LocalDateTime inicio, LocalDateTime fim, UUID turmaId) {
        return pedidoUniformeRepository.findByPedidoDataEfetivadaBetweenAndPedidoAlunoTurmaId(inicio, fim, turmaId);
    }

    private Map<UUID, Integer> consolidarItens(List<RequestItemSaidaDTO> itensDto) {
        Map<UUID, Integer> consolidado = new LinkedHashMap<>();
        for (RequestItemSaidaDTO item : itensDto) {
            consolidado.merge(item.uniformeId(), item.quantidade(), this::somarQuantidades);
        }
        return consolidado;
    }

    private Integer somarQuantidades(Integer a, Integer b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException e) {
            throw new BadRequestException("Quantidade total do item excede o limite permitido");
        }
    }

    private PedidoUniforme buscarPedidoUniformeOuFalhar(UUID id) {
        return pedidoUniformeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item do pedido não encontrado com o ID: " + id));
    }

    private ResponsePedidoUniformeDTO toResponseDTO(PedidoUniforme pedidoUniforme) {
        Uniforme uniforme = pedidoUniforme.getUniforme();
        return new ResponsePedidoUniformeDTO(
                pedidoUniforme.getId(),
                pedidoUniforme.getPedido().getId(),
                uniforme.getId(),
                uniforme.getTipoUniforme().getTipo(),
                uniforme.getTamanho(),
                uniforme.getSexo(),
                pedidoUniforme.getQuantidade()
        );
    }
}