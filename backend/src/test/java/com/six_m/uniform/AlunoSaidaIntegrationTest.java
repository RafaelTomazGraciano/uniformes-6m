package com.six_m.uniform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Saída (desligamento) do aluno de ponta a ponta: o histórico de entregas
 * continua visível, o aluno não recebe novas entregas e não pode ser excluído.
 *
 * O container do Postgres é recriado a cada classe; a propriedade própria e o
 * {@link DirtiesContext} impedem que um contexto Spring em cache aponte para
 * o container de outra classe.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "uniform.teste.contexto=aluno-saida")
@DirtiesContext
class AlunoSaidaIntegrationTest extends AbstractIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String token;
    private String turmaId;
    private String uniformeId;
    private String alunoComEntregaId;
    private String alunoSemEntregaId;

    @Test
    @Order(1)
    void devePrepararDados() throws Exception {
        rest.postForEntity(baseUrl() + "/usuario/registrar",
                requisicao("""
                        {"nome":"Clara","email":"clara@teste.com","senha":"123456"}
                        """, null), String.class);
        ResponseEntity<String> login = rest.postForEntity(baseUrl() + "/auth/login",
                requisicao("""
                        {"email":"clara@teste.com","senha":"123456"}
                        """, null), String.class);
        token = campo(login.getBody(), "token");

        String tipoId = campo(rest.postForEntity(baseUrl() + "/tipo-uniforme",
                requisicao("""
                        {"tipo":"Camiseta","estoqueBaixo":2}
                        """, token), String.class).getBody(), "id");

        ResponseEntity<String> lote = rest.postForEntity(baseUrl() + "/lote", requisicao("""
                {
                  "chaveAcesso": "12345678901234567890123456789012345678901234",
                  "fornecedor": "Fornecedor Teste",
                  "dataEntrega": "2026-03-10T08:00:00",
                  "itens": [ { "tipoUniformeId": "%s", "tamanho": "M", "sexo": "MASCULINO", "quantidade": 20 } ]
                }
                """.formatted(tipoId), token), String.class);
        assertThat(lote.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        uniformeId = get("/uniforme?page=0&size=10").get("content").get(0).get("id").asText();

        turmaId = campo(rest.postForEntity(baseUrl() + "/turma",
                requisicao("""
                        {"nome":"6º Ano B","turno":"DIURNO","ensino":"FUNDAMENTAL"}
                        """, token), String.class).getBody(), "id");

        alunoComEntregaId = criarAluno("João Conceição");
        alunoSemEntregaId = criarAluno("Maria Silva");

        assertThat(criarPedido(alunoComEntregaId).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Order(2)
    void deveRecusarExcluirAlunoComEntregas() throws Exception {
        ResponseEntity<String> response = exchange("/aluno/" + alunoComEntregaId, HttpMethod.DELETE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(campo(response.getBody(), "message"))
                .isEqualTo("Este aluno tem entregas registradas. Use 'Registrar saída' para manter o histórico.");
    }

    @Test
    @Order(3)
    void deveRegistrarSaidaDoAluno() throws Exception {
        ResponseEntity<String> response = exchange("/aluno/" + alunoComEntregaId + "/saida", HttpMethod.PATCH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode aluno = objectMapper.readTree(response.getBody());
        assertThat(aluno.get("ativo").asBoolean()).isFalse();
        assertThat(aluno.get("dataSaida").isNull()).isFalse();

        assertThat(exchange("/aluno/" + alunoComEntregaId + "/saida", HttpMethod.PATCH).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(4)
    void deveManterHistoricoDoAlunoQueSaiu() throws Exception {
        JsonNode lista = get("/pedido?sort=dataEfetivada,desc");
        assertThat(lista.get("totalElements").asInt()).isEqualTo(1);
        JsonNode pedido = lista.get("content").get(0);
        assertThat(pedido.get("alunoNome").asText()).isEqualTo("João Conceição");
        assertThat(pedido.get("alunoAtivo").asBoolean()).isFalse();

        assertThat(get("/pedido?alunoId=" + alunoComEntregaId).get("totalElements").asInt()).isEqualTo(1);
        assertThat(get("/aluno/" + alunoComEntregaId).get("ativo").asBoolean()).isFalse();

        HttpEntity<Void> entidade = new HttpEntity<>(headersComToken(token));
        ResponseEntity<byte[]> relatorio = rest.exchange(
                baseUrl() + "/relatorio/saida?tipo=MES&anoInicio=2026&mesInicio=3",
                HttpMethod.GET, entidade, byte[].class);
        assertThat(relatorio.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(relatorio.getBody()).isNotEmpty();
    }

    @Test
    @Order(5)
    void deveRecusarNovaEntregaParaAlunoQueSaiu() throws Exception {
        ResponseEntity<String> response = criarPedido(alunoComEntregaId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(campo(response.getBody(), "message")).contains("não pode receber novas entregas");
    }

    @Test
    @Order(6)
    void deveFiltrarAlunosPorSituacaoNomeETurma() throws Exception {
        assertThat(nomes(get("/aluno?ativo=true"))).containsExactly("Maria Silva");
        assertThat(nomes(get("/aluno?ativo=false"))).containsExactly("João Conceição");
        assertThat(nomes(get("/aluno?sort=nome,asc"))).containsExactly("João Conceição", "Maria Silva");

        assertThat(nomes(get("/aluno?nome=CONCEICAO"))).containsExactly("João Conceição");
        assertThat(nomes(get("/aluno?nome=joão"))).containsExactly("João Conceição");
        assertThat(nomes(get("/aluno?nome=6º ano&sort=nome,asc"))).containsExactly("João Conceição", "Maria Silva");
        assertThat(nomes(get("/aluno?nome=zzzz"))).isEmpty();
        assertThat(nomes(get("/aluno?nome=100%"))).isEmpty();

        assertThat(nomes(get("/aluno?turmaId=" + turmaId + "&ativo=true"))).containsExactly("Maria Silva");
        assertThat(nomes(get("/aluno?turmaId=" + UUID.randomUUID()))).isEmpty();
    }

    @Test
    @Order(7)
    void deveReativarAluno() throws Exception {
        ResponseEntity<String> response = exchange("/aluno/" + alunoComEntregaId + "/reativar", HttpMethod.PATCH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode aluno = objectMapper.readTree(response.getBody());
        assertThat(aluno.get("ativo").asBoolean()).isTrue();
        assertThat(aluno.get("dataSaida").isNull()).isTrue();
        assertThat(criarPedido(alunoComEntregaId).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Order(8)
    void devePermitirExcluirAlunoSemEntregas() {
        assertThat(exchange("/aluno/" + alunoSemEntregaId, HttpMethod.DELETE).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange("/aluno/" + alunoSemEntregaId, HttpMethod.GET).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /* Dados gravados antes da saída existir: o aluno foi excluído (soft delete)
       com entregas. A listagem precisa continuar respondendo. */
    @Test
    @Order(9)
    void naoDeveQuebrarListagemQuandoAlunoDaEntregaFoiExcluido() throws Exception {
        jdbcTemplate.update("UPDATE aluno SET deletado = true WHERE id = ?::uuid", alunoComEntregaId);

        ResponseEntity<String> response = exchange("/pedido?sort=dataEfetivada,desc", HttpMethod.GET);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode content = objectMapper.readTree(response.getBody()).get("content");
        assertThat(content.size()).isEqualTo(2);
        assertThat(content.get(0).get("alunoNome").asText()).isEqualTo("Aluno removido");
        assertThat(content.get(0).get("alunoId").asText()).isEqualTo(alunoComEntregaId);

        jdbcTemplate.update("UPDATE aluno SET deletado = false WHERE id = ?::uuid", alunoComEntregaId);
    }

    private String criarAluno(String nome) throws Exception {
        ResponseEntity<String> response = rest.postForEntity(baseUrl() + "/aluno",
                requisicao("""
                        {"nome":"%s","turmaId":"%s"}
                        """.formatted(nome, turmaId), token), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return campo(response.getBody(), "id");
    }

    private ResponseEntity<String> criarPedido(String alunoId) {
        return rest.postForEntity(baseUrl() + "/pedido", requisicao("""
                {
                  "alunoId": "%s",
                  "dataEfetivada": "2026-03-15T09:00:00",
                  "itens": [ { "uniformeId": "%s", "quantidade": 1 } ]
                }
                """.formatted(alunoId, uniformeId), token), String.class);
    }

    private JsonNode get(String caminho) throws Exception {
        ResponseEntity<String> response = exchange(caminho, HttpMethod.GET);
        assertThat(response.getStatusCode()).as("GET " + caminho).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody());
    }

    private ResponseEntity<String> exchange(String caminho, HttpMethod metodo) {
        return rest.exchange(baseUrl() + caminho, metodo, new HttpEntity<>(headersComToken(token)), String.class);
    }

    private List<String> nomes(JsonNode pagina) {
        List<String> nomes = new ArrayList<>();
        pagina.get("content").forEach(aluno -> nomes.add(aluno.get("nome").asText()));
        return nomes;
    }

    private HttpEntity<String> requisicao(String corpo, String tokenOpcional) {
        HttpHeaders headers = tokenOpcional != null ? headersComToken(tokenOpcional) : new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(corpo, headers);
    }

    private HttpHeaders headersComToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String campo(String jsonBody, String nomeCampo) throws Exception {
        return objectMapper.readTree(jsonBody).get(nomeCampo).asText();
    }
}
