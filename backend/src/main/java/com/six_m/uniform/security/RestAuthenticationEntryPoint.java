package com.six_m.uniform.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Responde 401 (e não o 403 padrão do Spring Security) quando a requisição chega
 * sem token, com token inválido ou expirado. O frontend usa o 401 para encerrar a sessão.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String MENSAGEM = "Sessão expirada ou inválida. Entre novamente.";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + MENSAGEM + "\"}");
    }
}
