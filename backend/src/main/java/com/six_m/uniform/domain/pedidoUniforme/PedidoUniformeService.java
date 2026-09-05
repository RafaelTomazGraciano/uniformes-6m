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
        validarItensSemDuplicidade(itensDto);

        List<PedidoUniforme> itens = new ArrayList<>();
        for (RequestItemSaidaDTO itemDto : itensDto) {
            Uniforme uniforme = uniformeService.buscarUniformeEntidade(itemDto.uniformeId());

            PedidoUniforme pedidoUniforme = PedidoUniforme.builder()
                    .pedido(pedido)
                    .uniforme(uniforme)
                    .quantidade(itemDto.quantidade())
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

    private void validarItensSemDuplicidade(List<RequestItemSaidaDTO> itensDto) {
        Set<UUID> uniformesVistos = new HashSet<>();
        for (RequestItemSaidaDTO item : itensDto) {
            if (!uniformesVistos.add(item.uniformeId())) {
                throw new BadRequestException("Item duplicado no pedido: mesmo uniforme informado mais de uma vez");
            }
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