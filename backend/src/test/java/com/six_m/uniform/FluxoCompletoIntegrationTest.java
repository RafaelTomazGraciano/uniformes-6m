package com.six_m.uniform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FluxoCompletoIntegrationTest extends AbstractIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;
    private String turmaId;
    private String alunoId;
    private String tipoUniformeId;
    private String uniformeId;

    @Test
    @Order(1)
    void deveRegistrarUsuario() {
        ResponseEntity<String> response = rest.postForEntity(
                baseUrl() + "/usuario/registrar",
                requisicao("""
                        {"nome":"Rafael","email":"rafael@teste.com","senha":"123456"}
                        """, null),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Order(2)
    void deveFazerLoginEObterToken() throws Exception {
        ResponseEntity<String> response = rest.postForEntity(
                baseUrl() + "/auth/login",
                requisicao("""
                        {"email":"rafael@teste.com","senha":"123456"}
                        """, null),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        token = campo(response.getBody(), "token");
        assertThat(token).isNotNull();
    }

    @Test
    @Order(3)
    void deveCriarTipoUniforme() throws Exception {
        ResponseEntity<String> response = rest.postForEntity(
                baseUrl() + "/tipo-uniforme",
                requisicao("""
                        {"tipo":"Camiseta"}
                        """, token),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        tipoUniformeId = campo(response.getBody(), "id");
        assertThat(tipoUniformeId).isNotNull();
    }

    @Test
    @Order(4)
    void deveCriarLoteEDarEntradaNoEstoque() throws Exception {
        String corpo = """
                {
                  "chaveAcesso": "12345678901234567890123456789012345678901234",
                  "fornecedor": "Fornecedor Teste",
                  "dataEntrega": "2026-03-10T08:00:00",
                  "itens": [
                    { "tipoUniformeId": "%s", "tamanho": "M", "sexo": "MASCULINO", "quantidade": 20 }
                  ]
                }
                """.formatted(tipoUniformeId);

        ResponseEntity<String> response = rest.postForEntity(baseUrl() + "/lote", requisicao(corpo, token), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode itens = objectMapper.readTree(response.getBody()).get("itens");
        assertThat(itens.get(0).get("quantidade").asInt()).isEqualTo(20);

        uniformeId = extrairUniformeIdDoEstoque();
        assertThat(uniformeId).as("ID do uniforme criado no estoque").isNotNull();
    }

    private String extrairUniformeIdDoEstoque() throws Exception {
        HttpEntity<Void> entidade = new HttpEntity<>(headersComToken(token));
        ResponseEntity<String> response = rest.exchange(
                baseUrl() + "/uniforme?page=0&size=10", HttpMethod.GET, entidade, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody()).get("content").get(0).get("id").asText();
    }

    @Test
    @Order(5)
    void deveCriarTurmaEAluno() throws Exception {
        ResponseEntity<String> turmaResponse = rest.postForEntity(
                baseUrl() + "/turma",
                requisicao("""
                        {"nome":"Turma A","turno":"DIURNO","ensino":"FUNDAMENTAL"}
                        """, token),
                String.class);

        assertThat(turmaResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        turmaId = campo(turmaResponse.getBody(), "id");

        ResponseEntity<String> alunoResponse = rest.postForEntity(
                baseUrl() + "/aluno",
                requisicao("""
                        {"nome":"João da Silva","turmaId":"%s"}
                        """.formatted(turmaId), token),
                String.class);

        assertThat(alunoResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        alunoId = campo(alunoResponse.getBody(), "id");
    }

    @Test
    @Order(6)
    void deveCriarPedidoEDecrementarEstoque() throws Exception {
        String corpo = """
                {
                  "alunoId": "%s",
                  "dataEfetivada": "2026-03-15T09:00:00",
                  "itens": [
                    { "uniformeId": "%s", "quantidade": 5 }
                  ]
                }
                """.formatted(alunoId, uniformeId);

        ResponseEntity<String> pedidoResponse = rest.postForEntity(baseUrl() + "/pedido", requisicao(corpo, token), String.class);
        assertThat(pedidoResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        HttpEntity<Void> entidade = new HttpEntity<>(headersComToken(token));
        ResponseEntity<String> uniformeResponse = rest.exchange(
                baseUrl() + "/uniforme/" + uniformeId, HttpMethod.GET, entidade, String.class);

        assertThat(uniformeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        int quantidadeAtual = objectMapper.readTree(uniformeResponse.getBody()).get("quantidade").asInt();
        assertThat(quantidadeAtual).isEqualTo(15); // 20 (entrada) - 5 (saída)
    }

    @Test
    @Order(7)
    void deveGerarRelatorioDeEstoqueEmPdf() throws Exception {
        HttpEntity<Void> entidade = new HttpEntity<>(headersComToken(token));
        ResponseEntity<byte[]> response = rest.exchange(
                baseUrl() + "/relatorio/estoque", HttpMethod.GET, entidade, byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        byte[] pdf = response.getBody();
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");

        salvarPdfParaInspecao(pdf, "relatorio-estoque.pdf");
    }

    @Test
    @Order(8)
    void deveGerarRelatorioDeTransacoesComPeriodoValido() throws Exception {
        HttpEntity<Void> entidade = new HttpEntity<>(headersComToken(token));
        ResponseEntity<byte[]> response = rest.exchange(
                baseUrl() + "/relatorio/transacoes?tipo=MES&anoInicio=2026&mesInicio=3",
                HttpMethod.GET, entidade, byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);

        salvarPdfParaInspecao(response.getBody(), "relatorio-transacoes.pdf");
    }

    @Test
    @Order(9)
    void deveRetornar404QuandoNaoHaTransacoesNoPeriodo() throws Exception {
        HttpEntity<Void> entidade = new HttpEntity<>(headersComToken(token));
        ResponseEntity<String> response = rest.exchange(
                baseUrl() + "/relatorio/transacoes?tipo=ANO&anoInicio=2020",
                HttpMethod.GET, entidade, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(campo(response.getBody(), "message"))
                .isEqualTo("Não há transações de uniformes no período informado");
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

    private void salvarPdfParaInspecao(byte[] pdf, String nomeArquivo) throws java.io.IOException {
        java.nio.file.Path destino = java.nio.file.Path.of("build", "test-pdfs", nomeArquivo);
        java.nio.file.Files.createDirectories(destino.getParent());
        java.nio.file.Files.write(destino, pdf);
        System.out.println("PDF salvo em: " + destino.toAbsolutePath());
    }
}