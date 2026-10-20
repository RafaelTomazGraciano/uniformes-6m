package com.six_m.uniform.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.domain.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recoverToken(request);

        if (token != null) {
            try {
                Optional<String> subject = jwtTokenService.validateToken(token);
                subject.ifPresent(this::autenticar);
            } catch (JWTVerificationException exception) {
                // Token inválido ou expirado: segue sem autenticar
            }
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            UserDetailsImpl userDetails = new UserDetailsImpl(usuario);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
    }

    private String recoverToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }

}