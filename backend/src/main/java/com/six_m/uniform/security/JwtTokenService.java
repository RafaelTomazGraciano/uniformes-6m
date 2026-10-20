package com.six_m.uniform.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class JwtTokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private final String ISSUER = "6M-Uniformes";

    public String generateToken(UserDetailsImpl user) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plusSeconds(36000))
                    .withSubject(user.getUsername())
                    .sign(algorithm);
        }catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public Optional<String> validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var subject = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
            return Optional.ofNullable(subject);
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Token inválido ou expirado");
        }
    }

}
