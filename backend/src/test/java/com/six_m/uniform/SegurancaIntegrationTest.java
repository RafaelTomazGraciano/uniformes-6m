package com.six_m.uniform;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Container e contexto próprios (com @DirtiesContext) para não reaproveitar o contexto
 * em cache de outra classe de integração apontando para um container já encerrado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SegurancaIntegrationTest {

    private static final String MENSAGEM_SESSAO = "Sessão expirada ou inválida. Entre novamente.";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"));

    @LocalServerPort
    private int port;

    @Value("${api.security.token.secret}")
    private String secret;

    private final TestRestTemplate rest = new TestRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;

    @Test
    @Order(1)
    void deveRegistrarEAutenticar() throws Exception {
        ResponseEntity<String> registro = rest.postForEntity(url("/usuario/registrar"), requisicao("""
                {"nome":"Maria","email":"maria@teste.com","senha":"123456"}
                """, null), String.class);
        assertThat(registro.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        token = login("maria@teste.com", "123456");
        assertThat(token).isNotBlank();
    }

    @Test
    @Order(2)
    void deveRetornar401SemToken() throws Exception {
        ResponseEntity<String> response = rest.getForEntity(url("/turma"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(mensagem(response)).isEqualTo(MENSAGEM_SESSAO);
    }

    @Test
    @Order(3)
    void deveRetornar401ComTokenInvalido() throws Exception {
        ResponseEntity<String> response = get("/turma", "token.invalido.aqui");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(mensagem(response)).isEqualTo(MENSAGEM_SESSAO);
    }

    @Test
    @Order(4)
    void deveRetornar401ComTokenExpirado() throws Exception {
        String expirado = JWT.create()
                .withIssuer("6M-Uniformes")
                .withIssuedAt(Instant.now().minusSeconds(7200))
                .withExpiresAt(Instant.now().minusSeconds(3600))
                .withSubject("maria@teste.com")
                .sign(Algorithm.HMAC256(secret));

        ResponseEntity<String> response = get("/turma", expirado);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(mensagem(response)).isEqualTo(MENSAGEM_SESSAO);
    }

    @Test
    @Order(5)
    void deveAceitarTokenValido() {
        assertThat(get("/turma", token).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(6)
    void devePermitirAtualizarMantendoOProprioEmail() throws Exception {
        ResponseEntity<String> response = rest.exchange(url("/usuario/atualizar"), HttpMethod.PUT, requisicao("""
                {"nome":"Maria Silva","email":"maria@teste.com"}
                """, token), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(response.getBody()).get("nome").asText()).isEqualTo("Maria Silva");
    }

    @Test
    @Order(7)
    void naoDeveTrocarSenhaComSenhaAtualIncorreta() throws Exception {
        ResponseEntity<String> response = trocarSenha("maria@teste.com", "errada", "novaSenha1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mensagem(response)).isEqualTo("Senha atual incorreta");
    }

    @Test
    @Order(8)
    void naoDeveRevelarEmailInexistenteAoTrocarSenha() throws Exception {
        ResponseEntity<String> response = trocarSenha("ninguem@teste.com", "123456", "novaSenha1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mensagem(response)).isEqualTo("Senha atual incorreta");
    }

    @Test
    @Order(9)
    void deveExigirSenhaAtualNoCorpo() {
        ResponseEntity<String> response = rest.exchange(url("/usuario/trocar-senha"), HttpMethod.PUT, requisicao("""
                {"email":"maria@teste.com","novaSenha":"novaSenha1"}
                """, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(10)
    void deveTrocarSenhaComSenhaAtualCorreta() throws Exception {
        ResponseEntity<String> response = trocarSenha("maria@teste.com", "123456", "novaSenha1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login("maria@teste.com", "novaSenha1")).isNotBlank();
    }

    private ResponseEntity<String> trocarSenha(String email, String senhaAtual, String novaSenha) {
        String corpo = """
                {"email":"%s","senhaAtual":"%s","novaSenha":"%s"}
                """.formatted(email, senhaAtual, novaSenha);
        return rest.exchange(url("/usuario/trocar-senha"), HttpMethod.PUT, requisicao(corpo, null), String.class);
    }

    private String login(String email, String senha) throws Exception {
        ResponseEntity<String> response = rest.postForEntity(url("/auth/login"), requisicao("""
                {"email":"%s","senha":"%s"}
                """.formatted(email, senha), null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody()).get("token").asText();
    }

    private ResponseEntity<String> get(String caminho, String tokenAutorizacao) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenAutorizacao);
        return rest.exchange(url(caminho), HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private HttpEntity<String> requisicao(String corpo, String tokenOpcional) {
        HttpHeaders headers = new HttpHeaders();
        if (tokenOpcional != null) {
            headers.setBearerAuth(tokenOpcional);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(corpo, headers);
    }

    private String mensagem(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody()).get("message").asText();
    }

    private String url(String caminho) {
        return "http://localhost:" + port + "/api" + caminho;
    }
}
