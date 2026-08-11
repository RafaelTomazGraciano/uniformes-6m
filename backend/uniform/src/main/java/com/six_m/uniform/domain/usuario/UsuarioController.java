package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.usuario.dto.RequestAtualizarUsuarioDTO;
import com.six_m.uniform.domain.usuario.dto.RequestRegistrarUsuarioDTO;
import com.six_m.uniform.domain.usuario.dto.ResponseUsuarioDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registrar")
    public ResponseEntity<ResponseUsuarioDTO> registrarUsuario(@Valid @RequestBody RequestRegistrarUsuarioDTO request) {
        ResponseUsuarioDTO response = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/atualizar")
    public ResponseEntity<ResponseUsuarioDTO> atualizarUsuario(@Valid @RequestBody RequestAtualizarUsuarioDTO request) {
        ResponseUsuarioDTO response = usuarioService.atualizarUsuario(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/deletar")
    public ResponseEntity<String> deletarUsuario() {
        String response = usuarioService.deletarUsuario();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
