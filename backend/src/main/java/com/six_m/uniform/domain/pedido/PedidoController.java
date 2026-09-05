package com.six_m.uniform.domain.pedido;

import com.six_m.uniform.domain.pedido.dto.RequestAtualizarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.RequestCriarPedidoDTO;
import com.six_m.uniform.domain.pedido.dto.ResponsePedidoDTO;
import com.six_m.uniform.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/pedido")
@Tag(name = "Pedido", description = "Cadastro de pedidos de uniforme para alunos. Ao criar/atualizar, o estoque é ajustado automaticamente")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Criar pedido", description = "Cadastra um pedido com um ou mais itens de uniforme, decrementando o estoque de cada um. O usuário responsável é identificado pelo token, não pelo body")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, item duplicado no pedido, ou quantidade solicitada maior que o estoque disponível"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Aluno ou uniforme não encontrados")
    })
    public ResponseEntity<ResponsePedidoDTO> criarPedido(
            @Valid @RequestBody RequestCriarPedidoDTO request,
            @AuthenticationPrincipal(expression = "usuario") Usuario usuario) {
        ResponsePedidoDTO response = pedidoService.criarPedido(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Retorna uma lista paginada de pedidos. Ordenação disponível por: dataEfetivada (ex: sort=dataEfetivada,desc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de ordenação inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponsePedidoDTO>> buscarTodosPedidos(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pedidoService.buscarTodosPedidos(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ResponsePedidoDTO> buscarPedido(
            @Parameter(description = "Identificador do pedido") @PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.buscarPedido(id));
    }

    @PutMapping("{id}")
    @Operation(summary = "Atualizar pedido", description = "Substitui os itens do pedido: devolve ao estoque a quantidade dos itens antigos e decrementa a dos novos. O usuário que realizou a edição fica registrado no pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, item duplicado no pedido, ou quantidade solicitada maior que o estoque disponível"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Pedido, aluno ou uniforme não encontrados")
    })
    public ResponseEntity<ResponsePedidoDTO> atualizarPedido(
            @Parameter(description = "Identificador do pedido") @PathVariable UUID id,
            @Valid @RequestBody RequestAtualizarPedidoDTO request,
            @AuthenticationPrincipal(expression = "usuario") Usuario usuario) {
        return ResponseEntity.ok(pedidoService.atualizarPedido(id, request, usuario));
    }
}