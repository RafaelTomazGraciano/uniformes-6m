package com.six_m.uniform.domain.itemLote;

import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
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
@RequestMapping("api/item-lote")
@Tag(name = "Item de Lote", description = "Cadastro e gestão dos itens que compõem um lote de entrega")
@SecurityRequirement(name = "bearerAuth")
public class ItemLoteController {

    private final ItemLoteService itemLoteService;

    @GetMapping
    @Operation(summary = "Listar itens de lote", description = "Retorna uma lista paginada de itens de lote. Ordenação disponível por: tamanho, quantidade, sexo (ex: sort=quantidade,desc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de ordenação inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<Page<ResponseItemLoteDTO>> buscarTodosItensLote(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(itemLoteService.buscarTodosItensLote(pageable));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar item de lote por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item de lote encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Item de lote não encontrado")
    })
    public ResponseEntity<ResponseItemLoteDTO> buscarItemLote(
            @Parameter(description = "Identificador do item de lote") @PathVariable UUID id) {
        return ResponseEntity.ok(itemLoteService.buscarItemLote(id));
    }
}