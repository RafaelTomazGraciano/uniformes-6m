# 6M Uniformes - Backend
 
Sistema de gestão de uniformes escolares. API REST desenvolvida em Java, responsável por autenticação, gestão de usuários e gestão de uniformes.
 
## Tecnologias
 
- **Java 25**
- **Spring Boot** (Web, Data JPA, Security, Validation)
- **PostgreSQL 18**
- **Flyway** (migrações de banco)
- **JWT** (autenticação stateless via `java-jwt`)
- **Docker / Docker Compose**

## Como rodar o projeto
 
### Pré-requisitos
 
- Docker instalado
- Arquivo `.env` configurado na raiz do projeto (veja `.env.example`)

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
 
Todos os endpoints estão disponíveis sob:
 
```
http://localhost:8080/api
```
 
Os exemplos abaixo usam caminhos relativos a essa base (ex: `/auth/login` = `http://localhost:8080/api/auth/login`).
 
---
 
## Autenticação
 
A API usa **JWT (JSON Web Token)** com sessão stateless - não há cookies de sessão, o token deve ser enviado manualmente em toda requisição autenticada.
 
### Como autenticar
 
1. Chame `POST /auth/login` (ou `POST /usuario/registrar`, que também autentica um novo usuário) para obter o token.
2. Envie o token em todas as próximas requisições, no header:
```
Authorization: Bearer <token>
```
 
### Quais endpoints são públicos
 
Apenas estes dois endpoints **não exigem token**:
 
| Método | Endpoint |
|---|---|
| `POST` | `/auth/login` |
| `POST` | `/usuario/registrar` |
 
**Todos os demais endpoints exigem o header `Authorization: Bearer <token>`.** Isso inclui a listagem e busca de escolas - se o front tentar chamar `GET /escola` sem token, a API retorna `401 Unauthorized`.
 
### Expiração do token
 
O token expira em **10 horas** (36000 segundos) a partir da emissão. Após expirar, a API retorna `401 Unauthorized` e o usuário precisa logar novamente.
 
---
 
## Endpoints
 
### Auth
 
#### Login
 
```
POST /auth/login
```
 
Autentica um usuário existente e retorna o token JWT.
 
**Request body:**
 
```json
{
  "email": "teste@teste.com",
  "senha": "123456"
}
```
 
**Response `200 OK`:**
 
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI2TS1Vbmlmb3JtZXMi..."
}
```

---
 
### Usuário
 
#### Registrar usuário
 
```
POST /usuario/registrar
```
 
Cria um novo usuário. Não exige autenticação.
 
**Request body:**
 
```json
{
  "nome": "teste",
  "email": "teste@teste.com",
  "senha": "123456"
}
```
 
**Response `201 Created`:**
 
```json
{
  "idUsuario": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nome": "teste",
  "email": "teste@teste.com"
}
```

---
 
#### Atualizar usuário
 
```
PUT /usuario/atualizar
```
 
**Requer token.** Atualiza nome e email do usuário autenticado. O usuário a ser atualizado é identificado pelo token, **não** pelo body - não é necessário (nem possível) enviar o ID ou o email atual.
 
**Headers:**
 
```
Authorization: Bearer <token>
```
 
**Request body:**
 
```json
{
  "nome": "testeAtualizado",
  "email": "testeAtualizado@email.com"
}
```
 
**Response `200 OK`:**
 
```json
{
  "idUsuario": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nome": "testeAtualizado",
  "email": "testeAtualizado@email.com"
}
```
 
---
 
#### Deletar usuário
 
```
DELETE /usuario/deletar
```
 
🔒 **Requer token.** Remove (soft delete) o usuário autenticado. O usuário é identificado pelo token - não há body nem parâmetros.
 
**Headers:**
 
```
Authorization: Bearer <token>
```
 
**Request body:** nenhum
 
**Response `200 OK`:**
 
```json
{
  "message": "Usuário deletado com sucesso"
}
```
 
---
 
### Escola
 
#### Listar escolas (paginado)
 
```
GET /escola?page=0&size=10
```
 
🔒 **Requer token.** Retorna uma lista paginada de escolas.
 
**Headers:**
 
```
Authorization: Bearer <token>
```
 
**Query params:**
 
| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
|---|---|:---:|:---:|---|
| `page` | number | ❌ | `0` | Número da página (começa em 0) |
| `size` | number | ❌ | `20` | Quantidade de itens por página |
 
**Response `200 OK`:**
 
```json
{
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "nome": "Escola Estadual João e Maria",
      "tipo": "PUBLICA",
      "endereco": "Rua das Flores, 123 - Centro"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```
 
O campo `tipo` é um enum (`TipoEscola`), com os seguintes valores possíveis:
 
| Valor | Descrição |
|---|---|
| `PUBLICA` | Escola pública |
| `TECNICA` | Escola técnica |
| `PARCEIRA` | Escola parceira |
| `CIVICO_MILITAR` | Escola cívico-militar |
| `MILITAR` | Escola militar |

 
---
 
#### Buscar escola por ID
 
```
GET /escola/{id}
```
 
🔒 **Requer token.**
 
**Headers:**
 
```
Authorization: Bearer <token>
```
 
**Path params:**
 
| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador da escola |
 
**Response `200 OK`:**
 
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nome": "Escola Estadual João e Maria",
  "tipo": "PUBLICA",
  "endereco": "Rua das Flores, 123 - Centro"
}
```
 
---

### Turma

#### Criar turma

```
POST /turma
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Request body:**

```json
{
  "nome": "Turma A",
  "turno": "DIURNO",
  "ensino": "FUNDAMENTAL"
}
```

**Response `201 Created`:**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nome": "Turma A",
  "turno": "DIURNO",
  "ensino": "FUNDAMENTAL"
}
```

---

#### Listar turmas (paginado)

```
GET /turma?page=0&size=10
```

🔒 **Requer token.** Retorna uma lista paginada de turmas.

**Headers:**

```
Authorization: Bearer <token>
```

**Query params:**

| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
|---|---|:---:|:---:|---|
| `page` | number | ❌ | `0` | Número da página (começa em 0) |
| `size` | number | ❌ | `20` | Quantidade de itens por página |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "nome": "Turma A",
      "turno": "DIURNO",
      "ensino": "FUNDAMENTAL"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

#### Buscar turma por ID

```
GET /turma/{id}
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador da turma |

**Response `200 OK`:**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nome": "Turma A",
  "turno": "DIURNO",
  "ensino": "FUNDAMENTAL"
}
```

---

#### Atualizar turma

```
PUT /turma/{id}
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador da turma |

**Request body:**

```json
{
  "nome": "Turma A - Atualizada",
  "turno": "NOTURNO",
  "ensino": "MEDIO"
}
```

**Response `200 OK`:**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nome": "Turma A - Atualizada",
  "turno": "NOTURNO",
  "ensino": "MEDIO"
}
```

---

#### Deletar turma

```
DELETE /turma/{id}
```

🔒 **Requer token.** Só é possível deletar uma turma se não houver nenhum aluno (não deletado) vinculado a ela.

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador da turma |

**Request body:** nenhum

**Response `200 OK`:**

```json
{
  "message": "Turma deletada com sucesso"
}
```

---

### Aluno

#### Criar aluno

```
POST /aluno
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Request body:**

```json
{
  "nome": "João da Silva",
  "turmaId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

**Response `201 Created`:**

```json
{
  "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "nome": "João da Silva",
  "turmaId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "turmaNome": "Turma A"
}
```
 
---

#### Listar alunos (paginado)

```
GET /aluno?page=0&size=10
```

🔒 **Requer token.** Retorna uma lista paginada de alunos.

**Headers:**

```
Authorization: Bearer <token>
```

**Query params:**

| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
|---|---|:---:|:---:|---|
| `page` | number | ❌ | `0` | Número da página (começa em 0) |
| `size` | number | ❌ | `20` | Quantidade de itens por página |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
      "nome": "João da Silva",
      "turmaId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "turmaNome": "Turma A"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```
 
---

#### Buscar aluno por ID

```
GET /aluno/{id}
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do aluno |

**Response `200 OK`:**

```json
{
  "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "nome": "João da Silva",
  "turmaId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "turmaNome": "Turma A"
}
```
 
---

#### Atualizar aluno

```
PUT /aluno/{id}
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do aluno |

**Request body:**

```json
{
  "nome": "João da Silva Atualizado",
  "turmaId": "9d7a9db1-a3ee-4c5e-9d99-9a01e4f2ee80"
}
```

**Response `200 OK`:**

```json
{
  "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "nome": "João da Silva Atualizado",
  "turmaId": "9d7a9db1-a3ee-4c5e-9d99-9a01e4f2ee80",
  "turmaNome": "Turma B"
}
```

> Se `turmaId` não corresponder a uma turma existente, a API retorna `404 Not Found`.
 
---

#### Deletar aluno

```
DELETE /aluno/{id}
```

🔒 **Requer token.** Remove (soft delete) o aluno.

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do aluno |

**Request body:** nenhum

**Response `200 OK`:**

```json
{
  "message": "Aluno deletado com sucesso"
}
```

---

### Tipo de Uniforme

#### Criar tipo de uniforme

```
POST /tipo-uniforme
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Request body:**

```json
{
  "tipo": "Camiseta"
}
```

**Response `201 Created`:**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tipo": "Camiseta"
}
```

---

#### Listar tipos de uniforme (paginado)

```
GET /tipo-uniforme?page=0&size=10
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Query params:**

| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
|---|---|:---:|:---:|---|
| `page` | number | ❌ | `0` | Número da página (começa em 0) |
| `size` | number | ❌ | `20` | Quantidade de itens por página |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "tipo": "Camiseta"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

#### Buscar tipo de uniforme por ID

```
GET /tipo-uniforme/{id}
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do tipo de uniforme |

**Response `200 OK`:**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tipo": "Camiseta"
}
```

---

#### Atualizar tipo de uniforme

```
PUT /tipo-uniforme/{id}
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do tipo de uniforme |

**Request body:**

```json
{
  "tipo": "Camiseta Polo"
}
```

**Response `200 OK`:**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tipo": "Camiseta Polo"
}
```

---

#### Deletar tipo de uniforme

```
DELETE /tipo-uniforme/{id}
```

🔒 **Requer token.** Só é possível deletar um tipo de uniforme se não houver nenhum uniforme ou item de lote vinculado a ele.

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do tipo de uniforme |

**Request body:** nenhum

**Response `200 OK`:**

```json
{
  "message": "Tipo de uniforme deletado com sucesso"
}
```

---

### Uniforme

#### Criar uniforme

```
POST /uniforme
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Request body:**

```json
{
  "tipoUniformeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tamanho": "M",
  "quantidade": 10,
  "sexo": "MASCULINO"
}
```

**Response `201 Created`:**

```json
{
  "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "tipoUniformeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tipoUniformeNome": "Camiseta",
  "tamanho": "M",
  "quantidade": 10,
  "sexo": "MASCULINO",
  "devolvido": false
}
```

---

#### Listar uniformes (paginado)

```
GET /uniforme?page=0&size=10
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Query params:**

| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
|---|---|:---:|:---:|---|
| `page` | number | ❌ | `0` | Número da página (começa em 0) |
| `size` | number | ❌ | `20` | Quantidade de itens por página |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
      "tipoUniformeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "tipoUniformeNome": "Camiseta",
      "tamanho": "M",
      "quantidade": 10,
      "sexo": "MASCULINO",
      "devolvido": false
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

#### Buscar uniforme por ID

```
GET /uniforme/{id}
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do uniforme |

**Response `200 OK`:**

```json
{
  "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "tipoUniformeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tipoUniformeNome": "Camiseta",
  "tamanho": "M",
  "quantidade": 10,
  "sexo": "MASCULINO",
  "devolvido": false
}
```

---

#### Atualizar uniforme

```
PUT /uniforme/{id}
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do uniforme |

**Request body:**

```json
{
  "tipoUniformeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tamanho": "GG",
  "quantidade": 20,
  "sexo": "FEMININO"
}
```

**Response `200 OK`:**

```json
{
  "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "tipoUniformeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tipoUniformeNome": "Camiseta",
  "tamanho": "GG",
  "quantidade": 20,
  "sexo": "FEMININO",
  "devolvido": false
}
```

---

#### Marcar uniforme como devolvido

```
PATCH /uniforme/{id}/devolver
```

🔒 **Requer token.**

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do uniforme |

**Request body:** nenhum

**Response `200 OK`:**

```json
{
  "id": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "tipoUniformeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tipoUniformeNome": "Camiseta",
  "tamanho": "M",
  "quantidade": 10,
  "sexo": "MASCULINO",
  "devolvido": true
}
```

---

#### Deletar uniforme

```
DELETE /uniforme/{id}
```

🔒 **Requer token.** Remove (soft delete) o uniforme.

**Headers:**

```
Authorization: Bearer <token>
```

**Path params:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do uniforme |

**Request body:** nenhum

**Response `200 OK`:**

```json
{
  "message": "Uniforme deletado com sucesso"
}
```

---

## Formato de erro

Todos os erros da API seguem um dos dois formatos abaixo.

### Erros de negócio (`400` e `404`)

Retornados quando uma regra de negócio falha (ex: email já cadastrado, recurso não encontrado). Corpo simples com uma mensagem:

```json
{
  "message": "Este email já está em uso"
}
```

| Status | Quando acontece |
|---|---|
| `404 Not Found` | Recurso não encontrado (ex: escola com ID inexistente) |
| `400 Bad Request` | Regra de negócio violada (ex: email duplicado) |

### Erros de validação (`400`)

Retornados quando os campos do `@RequestBody` não passam nas anotações de validação (`@NotEmpty`, `@Email`, `@Size`, etc). O corpo traz um `errors` com uma entrada por campo inválido:

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
 
## Enums do sistema
 
Referência de todos os enums usados no banco de dados. Alguns ainda não aparecem em nenhum endpoint (serão usados quando os módulos de uniforme e turma forem implementados), mas já ficam documentados aqui para o front se planejar.
 
### `TipoEscola`
 
| Valor |
|---|
| `PUBLICA` |
| `TECNICA` |
| `PARCEIRA` |
| `CIVICO_MILITAR` |
| `MILITAR` |
 
### `Sexo`
 
| Valor |
|---|
| `MASCULINO` |
| `FEMININO` |
 
### `Tamanho`
 
| Valor |
|---|
| `PP` |
| `P` |
| `M` |
| `G` |
| `GG` |
 
### `Turno`
 
| Valor |
|---|
| `DIURNO` |
| `VESPERTINO` |
| `NOTURNO` |
 
### `Ensino`
 
| Valor |
|---|
| `FUNDAMENTAL` |
| `MEDIO` |
| `TECNICO` |
 
---
 
## Padrões usados no código (referência para quem for consumir a API)
 
- IDs são sempre **UUID**, não números incrementais.
- Datas seguem o padrão ISO 8601 (ex: `"2025-06-17T14:30:00"`).
- Em endpoints autenticados, o usuário **nunca** é identificado por um campo no body - sempre pelo token.


## Implementação 

```java
@PostMapping
public ResponseEntity<?> criar(@RequestBody PedidoRequestDTO dto,
                                @AuthenticationPrincipal Usuario usuario) {
    // usuario vem do token, não do DTO
    pedidoService.criar(dto, usuario);
}
```

Docker PostgreSQL:

```bash
docker run -d --name uniform -e POSTGRES_DB=uniform -e POSTGRES_USER=uniform -e POSTGRES_PASSWORD=postgresUniform -p 5432:5432 -v uniform_data:/var/lib/postgresql postgres:18-alpine
```