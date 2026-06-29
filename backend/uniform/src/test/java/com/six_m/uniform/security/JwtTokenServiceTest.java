package com.six_m.uniform.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.six_m.uniform.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private UserDetailsImpl userDetails;

    @BeforeEach
    public void setUp() {
        jwtTokenService = new JwtTokenService();

        ReflectionTestUtils.setField(jwtTokenService, "secret", "test-secret-key");

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Teste")
                .email("teste@teste.com")
                .senha("123456")
                .build();
        userDetails = new UserDetailsImpl(usuario);
    }

    @Test
    void deveGerarTokenValido(){
        String token = jwtTokenService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deveValidarTokenGeradoCorretamente() {
        String token = jwtTokenService.generateToken(userDetails);

        Optional<String> subject = jwtTokenService.validateToken(token);

        assertTrue(subject.isPresent());
        assertEquals("teste@teste.com", subject.get());
    }

    @Test
    void deveLancarExcecaoParaTokenInvalido() {
        String tokenInvalido = "token.invalido.aqui";

        assertThrows(JWTVerificationException.class,
                () -> jwtTokenService.validateToken(tokenInvalido));
    }

    @Test
    void deveLancarExcecaoParaTokenComAssinaturaDiferente() {
        String token = jwtTokenService.generateToken(userDetails);

        // Troca a secret depois de gerar o token, simulando uma assinatura diferente
        ReflectionTestUtils.setField(jwtTokenService, "secret", "outra-secret-diferente");

        assertThrows(JWTVerificationException.class,
                () -> jwtTokenService.validateToken(token));
    }

}
