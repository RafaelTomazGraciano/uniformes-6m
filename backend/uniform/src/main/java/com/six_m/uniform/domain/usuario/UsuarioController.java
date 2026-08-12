package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.usuario.dto.RequestRegistrarUsuario;
import com.six_m.uniform.domain.usuario.dto.ResponseRegistrarUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registrar")
    public ResponseEntity<ResponseRegistrarUsuario> registrarUsuario(@Valid @RequestBody RequestRegistrarUsuario request) {
        ResponseRegistrarUsuario response = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
