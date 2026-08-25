package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.usuario.dto.LoginRequestDTO;
import com.six_m.uniform.domain.usuario.dto.LoginResponseDTO;
import com.six_m.uniform.security.JwtTokenService;
import com.six_m.uniform.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
@Tag(name = "Autenticação", description = "Login de usuários já cadastrados")
public class AutenticacaoController {

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida email e senha e retorna o token JWT para uso nos demais endpoints")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Email ou senha incorretos"),
            @ApiResponse(responseCode = "400", description = "Campos ausentes ou vazios")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.senha());
        var authentication = authenticationManager.authenticate(authToken);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtTokenService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}