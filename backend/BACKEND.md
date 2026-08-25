# 6M Uniformes - Backend

Sistema de gestão de uniformes escolares. API REST desenvolvida em Java, responsável por autenticação, gestão de usuários, escolas, turmas, alunos, uniformes, notas fiscais e lotes de entrega.

## Tecnologias

- **Java 25**
- **Spring Boot** (Web, Data JPA, Security, Validation)
- **PostgreSQL 18**
- **Flyway** (migrações de banco)
- **JWT** (autenticação stateless via `java-jwt`)
- **springdoc-openapi / Swagger** (documentação interativa da API)
- **Docker / Docker Compose**

## Como rodar o projeto

### Pré-requisitos

- Docker instalado
- Arquivo `.env` configurado dentro do backend (veja `.env.example`)

### Subindo o projeto pela primeira vez

```bash
docker compose up -d --build
```

A API estará disponível em `http://localhost:8080`.

### Verificando os logs

```bash
docker compose logs -f backend
docker compose logs -f db
```

### Atualizando o backend após mudanças no código

Sempre que alterar o código-fonte, é preciso rebuildar a imagem do backend:

```bash
docker compose up -d --build backend
```

Se quiser derrubar e recriar tudo do zero (útil se mexeu no `Dockerfile` ou `docker-compose.yml`):

```bash
docker compose down
docker compose up -d --build
```

### Apagando tudo, incluindo os dados do banco

```bash
docker compose down -v
```

⚠️ O comando acima remove o volume `uniform_data` e apaga todos os dados do Postgres.

---

## Base URL

```
http://localhost:8080/api
```

---

## Documentação da API (Swagger)

Toda a documentação de endpoints — request/response de cada rota, parâmetros, códigos de status — está no Swagger, gerado automaticamente a partir do código:

```
http://localhost:8080/swagger-ui.html
```

Clique em **"Authorize"** no topo da página, cole o token JWT (obtido em `POST /api/auth/login`) e todas as chamadas de teste feitas por ali já enviam o header `Authorization` automaticamente.

Nos endpoints de listagem (paginados), o campo `sort` aceita o formato `campo,direção` (ex: `quantidade,desc`) — clique em "Add string item" para adicionar cada critério de ordenação como um campo separado.

O JSON puro da especificação OpenAPI (útil para gerar clientes ou importar no Postman/Insomnia) fica em:

```
http://localhost:8080/v3/api-docs
```

---

## Autenticação

A API usa **JWT** com sessão stateless. Fluxo básico:

1. `POST /api/auth/login` retorna o token JWT. **`POST /api/usuario/registrar` não retorna token** — ele só cria o usuário; depois de registrar, é preciso chamar `/auth/login` separadamente para autenticar.
2. Envie esse token em toda requisição autenticada, no header:

```
Authorization: Bearer <token>
```

Apenas `POST /api/auth/login` e `POST /api/usuario/registrar` são públicos — todos os demais endpoints exigem o token. O token expira em **10 horas**.

---

## Formato de erro

Toda resposta de erro segue um dos dois formatos abaixo.

**Erro de negócio (`400`/`404`)** — recurso não encontrado ou regra violada (ex: email duplicado, recurso com vínculos que impedem exclusão, parâmetro de ordenação inválido):

```json
{
  "message": "Este email já está em uso"
}
```

**Erro de validação (`400`)** — campos do body que não passaram nas anotações de validação:

```json
{
  "message": "Validation failed",
  "errors": {
    "email": "O formato do email é inválido",
    "senha": "A senha deve ter no mínimo 6 caracteres"
  }
}
```

---

## Padrões usados no código

- IDs são sempre **UUID**, não números incrementais.
- Datas seguem o padrão ISO 8601 (ex: `"2025-06-17T14:30:00"`).
- Em endpoints autenticados, o usuário **nunca** é identificado por um campo no body — sempre pelo token:

```java
@PostMapping
public ResponseEntity<?> criar(@RequestBody PedidoRequestDTO dto,
                                @AuthenticationPrincipal Usuario usuario) {
    // usuario vem do token, não do DTO
    pedidoService.criar(dto, usuario);
}
```

---

## Rodando o Postgres isoladamente (sem o backend)

Útil para debug local:

```bash
docker run -d --name uniform -e POSTGRES_DB=uniform -e POSTGRES_USER=uniform -e POSTGRES_PASSWORD=postgresUniform -p 5432:5432 -v uniform_data:/var/lib/postgresql postgres:18-alpine
```