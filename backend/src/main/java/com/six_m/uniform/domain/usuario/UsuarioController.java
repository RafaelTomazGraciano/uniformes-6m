package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.usuario.dto.RequestAtualizarUsuarioDTO;
import com.six_m.uniform.domain.usuario.dto.RequestRegistrarUsuarioDTO;
import com.six_m.uniform.domain.usuario.dto.ResponseUsuarioDTO;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/usuario")
@Tag(name = "Usuário", description = "Cadastro e gestão da conta do usuário autenticado")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registrar")
    @Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário. Não exige autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Email já cadastrado ou dados inválidos")
    })
    public ResponseEntity<ResponseUsuarioDTO> registrarUsuario(@Valid @RequestBody RequestRegistrarUsuarioDTO request) {
        ResponseUsuarioDTO response = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/atualizar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualizar usuário autenticado", description = "Atualiza nome e email do usuário identificado pelo token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Novo email já está em uso ou dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<ResponseUsuarioDTO> atualizarUsuario(@Valid @RequestBody RequestAtualizarUsuarioDTO request) {
        ResponseUsuarioDTO response = usuarioService.atualizarUsuario(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/deletar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deletar usuário autenticado", description = "Remove (soft delete) o usuário identificado pelo token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    public ResponseEntity<MessageResponseDTO> deletarUsuario() {
        MessageResponseDTO response = usuarioService.deletarUsuario();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}