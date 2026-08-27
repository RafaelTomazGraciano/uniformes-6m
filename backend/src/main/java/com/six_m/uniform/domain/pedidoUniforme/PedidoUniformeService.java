package com.six_m.uniform.domain.pedidoUniforme;


import com.six_m.uniform.domain.pedidoUniforme.dto.ResponsePedidoUniformeDTO;
import com.six_m.uniform.domain.uniforme.Uniforme;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoUniformeService {

    private final PedidoUniformeRepository pedidoUniformeRepository;

    @Transactional(readOnly = true)
    public Page<ResponsePedidoUniformeDTO> buscarTodosPedidosUniforme(Pageable pageable) {
        return pedidoUniformeRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponsePedidoUniformeDTO buscarPedidoUniforme(UUID id) {
        return toResponseDTO(buscarPedidoUniformeOuFalhar(id));
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