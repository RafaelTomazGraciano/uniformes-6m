package com.six_m.uniform.domain.pedidoUniforme;

import com.six_m.uniform.domain.pedidoUniforme.dto.ResponsePedidoUniformeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/pedido-uniforme")
@Tag(name = "Pedido de Uniforme", description = "Consulta dos itens de pedido — o cadastro é feito automaticamente ao criar/atualizar um pedido")
@SecurityRequirement(name = "bearerAuth")
public class PedidoUniformeController {

    private final PedidoUniformeService pedidoUniformeService;

    @GetMapping
    @Operation(summary = "Listar itens de pedido", description = "Retorna uma lista paginada de itens de pedido de uniforme. Ordenação disponível por: quantidade (ex: sort=quantidade,desc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de ordenação inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponsePedidoUniformeDTO>> buscarTodosPedidosUniforme(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pedidoUniformeService.buscarTodosPedidosUniforme(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar item de pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item do pedido encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Item do pedido não encontrado")
    })
    public ResponseEntity<ResponsePedidoUniformeDTO> buscarPedidoUniforme(
            @Parameter(description = "Identificador do item do pedido") @PathVariable UUID id) {
        return ResponseEntity.ok(pedidoUniformeService.buscarPedidoUniforme(id));
    }
}