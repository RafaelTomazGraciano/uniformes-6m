# Documentação dos endpoints — API Uniformes 6M

Referência completa da API REST do backend. Os exemplos de resposta foram obtidos de chamadas reais à API rodando com os dados de demonstração (`backend/scripts/seed.mjs`); os UUIDs variam a cada banco.

> O Swagger (`http://localhost:8080/swagger-ui.html`) continua disponível para testar as chamadas pelo navegador. Este documento descreve o mesmo contrato com exemplos e com o comportamento real observado.

## Sumário

- [Convenções gerais](#convenções-gerais)
  - [Base URL](#base-url)
  - [Autenticação](#autenticação)
  - [Paginação e ordenação](#paginação-e-ordenação)
  - [Formato de erro](#formato-de-erro)
  - [Enums](#enums)
- [Autenticação](#1-autenticação) — `/api/auth`
- [Usuário](#2-usuário) — `/api/usuario`
- [Escola](#3-escola) — `/api/escola`
- [Turma](#4-turma) — `/api/turma`
- [Aluno](#5-aluno) — `/api/aluno`
- [Tipo de uniforme](#6-tipo-de-uniforme) — `/api/tipo-uniforme`
- [Uniforme (estoque)](#7-uniforme-estoque) — `/api/uniforme`
- [Lote (entrada de estoque)](#8-lote-entrada-de-estoque) — `/api/lote`
- [Item de lote](#9-item-de-lote) — `/api/item-lote`
- [Nota fiscal](#10-nota-fiscal) — `/api/nota-fiscal`
- [Pedido (entrega ao aluno)](#11-pedido-entrega-ao-aluno) — `/api/pedido`
- [Item de pedido](#12-item-de-pedido) — `/api/pedido-uniforme`
- [Relatórios em PDF ou CSV](#13-relatórios-em-pdf-ou-csv) — `/api/relatorio`
- [Requisição de novas peças](#14-requisição-de-novas-peças) — `/api/requisicao`
- [Resumo de todos os endpoints](#resumo-de-todos-os-endpoints)

---

## Convenções gerais

### Base URL

```
http://localhost:8080/api
```

Todas as rotas abaixo são relativas a esse endereço (ex.: `GET /turma` = `GET http://localhost:8080/api/turma`).

- IDs são sempre **UUID**.
- Datas seguem o ISO 8601 **sem fuso horário** (`LocalDateTime`), ex.: `"2026-10-18T21:10:50"`.
- Corpos de requisição e resposta são JSON (`Content-Type: application/json`), exceto os relatórios, que devolvem `application/pdf` ou `text/csv`.

### Autenticação

A API usa **JWT** em sessão stateless.

1. Faça `POST /auth/login` com email e senha e guarde o `token` retornado.
2. Envie o token em toda requisição protegida:

```
Authorization: Bearer <token>
```

- O token expira em **10 horas**.
- O usuário que executa uma ação (ex.: quem registrou uma entrega) é sempre identificado pelo token, nunca por um campo do corpo.
- **Rotas públicas** (sem token): `POST /auth/login`, `POST /usuario/registrar` e `PUT /usuario/trocar-senha`. Todas as demais exigem token.

**Requisição sem token ou com token inválido/expirado** recebe **`401 Unauthorized`** com corpo JSON (gerado por `security/RestAuthenticationEntryPoint`):

```json
{ "message": "Sessão expirada ou inválida. Entre novamente." }
```

O frontend trata qualquer `401` encerrando a sessão e voltando à tela de login com o aviso de sessão expirada. `403 Forbidden` fica reservado para usuário autenticado sem permissão (hoje não há perfis, então não ocorre na prática). `POST /auth/login` também responde `401` quando email ou senha estão errados, com `{"message":"Email ou senha incorretos"}`.

Exemplo com `curl`:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@uniformes6m.local","senha":"senha123"}' | jq -r .token)

curl -s http://localhost:8080/api/turma -H "Authorization: Bearer $TOKEN"
```

### Paginação e ordenação

Todos os endpoints de listagem (`GET` sem `{id}`) são paginados e aceitam os parâmetros de query abaixo:

| Parâmetro | Tipo    | Padrão | Descrição |
| --------- | ------- | ------ | --------- |
| `page`    | inteiro | `0`    | Número da página, começando em **0**. |
| `size`    | inteiro | `20`   | Quantidade de itens por página. |
| `sort`    | string  | —      | Critério de ordenação no formato `campo,direção` (`asc` ou `desc`). Pode ser repetido para ordenar por mais de um campo: `?sort=tamanho,asc&sort=quantidade,desc`. Os campos aceitos estão descritos em cada endpoint. |

Exemplo: `GET /uniforme?page=0&size=2&sort=quantidade,asc`

A resposta segue o formato de página do Spring Data:

```json
{
  "content": [ /* itens da página */ ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 2,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 2,
    "paged": true,
    "sort": { "empty": false, "sorted": true, "unsorted": false },
    "unpaged": false
  },
  "size": 2,
  "sort": { "empty": false, "sorted": true, "unsorted": false },
  "totalElements": 53,
  "totalPages": 27
}
```

| Campo              | Significado |
| ------------------ | ----------- |
| `content`          | Itens da página atual. |
| `number`           | Página atual (base 0). |
| `size`             | Tamanho de página pedido. |
| `numberOfElements` | Quantos itens vieram nesta página. |
| `totalElements`    | Total de registros. |
| `totalPages`       | Total de páginas. |
| `first` / `last`   | Se é a primeira / última página. |
| `empty`            | Se `content` está vazio. |

Nos exemplos de listagem deste documento, os campos de paginação são abreviados com `...` para destacar o `content`.

Ordenar por um campo que não existe retorna `400`:

```json
{ "message": "Parâmetro de ordenação inválido: foo" }
```

### Formato de erro

Todo erro tratado pela API tem um dos dois formatos:

**Erro de negócio ou de parâmetro** (`400`, `401`, `404`, `409`, `500`):

```json
{ "message": "Turma não encontrada com o ID: 00000000-0000-0000-0000-000000000000" }
```

**Erro de validação do corpo** (`400`): os campos que falharam ficam em `errors`.

```json
{
  "message": "Validation failed",
  "errors": {
    "senha": "Senha é obrigatório"
  }
}
```

Erros comuns a todos os endpoints:

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | `{id}` na rota não é um UUID válido | `Valor inválido para o parâmetro 'id'` |
| `400`  | JSON malformado ou valor de enum inexistente no corpo | `Corpo da requisição inválido ou malformado` |
| `400`  | Campo de `sort` inexistente | `Parâmetro de ordenação inválido: <campo>` |
| `401`  | Rota protegida sem token, ou token inválido/expirado | `Sessão expirada ou inválida. Entre novamente.` |
| `409`  | Duas operações tentaram criar a mesma combinação de uniforme (tipo + tamanho + sexo) ao mesmo tempo | `Este uniforme está sendo registrado por outra operação neste momento. Tente novamente` |
| `500`  | Erro inesperado | `Ocorreu um erro inesperado. Tente novamente mais tarde.` |

### Enums

| Enum        | Valores |
| ----------- | ------- |
| `Tamanho`   | `PP`, `P`, `M`, `G`, `GG` |
| `Sexo`      | `MASCULINO`, `FEMININO` |
| `Turno`     | `DIURNO`, `VESPERTINO`, `NOTURNO`, `INTEGRAL` |
| `Ensino`    | `FUNDAMENTAL`, `MEDIO`, `TECNICO` |
| `TipoEscola`| `PUBLICA`, `TECNICA`, `PARCEIRA`, `CIVICO_MILITAR`, `MILITAR` |
| `TipoFiltroRelatorio` | `MES`, `ANO` |
| `StatusRequisicao` | `PENDENTE`, `ATENDIDA`, `CANCELADA` |
| `SituacaoEstoque` | `OK`, `BAIXO`, `ESGOTADO` |

---

## 1. Autenticação

### `POST /auth/login` — Autenticar usuário

| | |
|---|---|
| **Autenticação** | Pública |
| **Descrição** | Valida email e senha e retorna o token JWT usado nos demais endpoints. |

**Corpo da requisição**

| Campo   | Tipo   | Obrigatório | Regras |
| ------- | ------ | ----------- | ------ |
| `email` | string | sim         | formato de email |
| `senha` | string | sim         | não vazia |

```json
{
  "email": "admin@uniformes6m.local",
  "senha": "senha123"
}
```

**Resposta `200 OK`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI2T..."
}
```

**Erros possíveis**

| Status | Quando | Exemplo |
| ------ | ------ | ------- |
| `400`  | Campos ausentes ou vazios | `{"message":"Validation failed","errors":{"senha":"Senha é obrigatório"}}` |
| `401`  | Email ou senha incorretos | `{"message":"Email ou senha incorretos"}` |

---

## 2. Usuário

### `POST /usuario/registrar` — Registrar novo usuário

| | |
|---|---|
| **Autenticação** | Pública |
| **Descrição** | Cria um usuário. **Não retorna token**: depois de registrar, é preciso chamar `POST /auth/login`. |

**Corpo da requisição**

| Campo   | Tipo   | Obrigatório | Regras |
| ------- | ------ | ----------- | ------ |
| `nome`  | string | sim         | não vazio |
| `email` | string | sim         | formato de email, único |
| `senha` | string | sim         | mínimo 6 caracteres |

```json
{
  "nome": "Marina Lopes",
  "email": "marina@escola.local",
  "senha": "senha123"
}
```

**Resposta `201 Created`**

```json
{
  "idUsuario": "fb804bdf-c281-4909-b5be-b755eeefdfe9",
  "nome": "Marina Lopes",
  "email": "marina@escola.local"
}
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Email já cadastrado | `Este email já está em uso` |
| `400`  | Validação | `Validation failed` + `errors` (ex.: `"senha": "A senha deve ter no mínimo 6 caracteres"`) |

### `PUT /usuario/atualizar` — Atualizar usuário autenticado

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Descrição** | Atualiza nome e email do usuário dono do token. |

**Corpo da requisição**

| Campo   | Tipo   | Obrigatório | Regras |
| ------- | ------ | ----------- | ------ |
| `nome`  | string | sim         | não vazio |
| `email` | string | sim (na prática) | formato de email; não pode estar em uso por **outro** usuário (reenviar o próprio email é aceito) |

```json
{
  "nome": "Marina Lopes Souza",
  "email": "marina.souza@escola.local"
}
```

**Resposta `200 OK`**

```json
{
  "idUsuario": "fb804bdf-c281-4909-b5be-b755eeefdfe9",
  "nome": "Marina Lopes Souza",
  "email": "marina.souza@escola.local"
}
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Email já usado por outro usuário | `Este email já está em uso` |
| `400`  | Validação | `Validation failed` + `errors` |
| `401`  | Sem token / token inválido ou expirado | `Sessão expirada ou inválida. Entre novamente.` |

> Observações: como o token identifica o usuário pelo email, depois de trocar o email é preciso fazer login de novo. Reenviar o próprio email atual (ex.: só mudar o nome) é aceito.

### `PUT /usuario/trocar-senha` — Trocar senha

| | |
|---|---|
| **Autenticação** | Pública |
| **Descrição** | Define uma nova senha para o usuário com o email informado, **desde que a senha atual esteja correta**. Não exige token porque também serve a quem não consegue entrar. |

**Corpo da requisição**

| Campo       | Tipo   | Obrigatório | Regras |
| ----------- | ------ | ----------- | ------ |
| `email`      | string | sim         | formato de email |
| `senhaAtual` | string | sim         | senha atual da conta |
| `novaSenha`  | string | sim         | mínimo 6 caracteres |

```json
{
  "email": "admin@uniformes6m.local",
  "senhaAtual": "senha123",
  "novaSenha": "novaSenha123"
}
```

**Resposta `200 OK`**

```json
{ "message": "Senha atualizada com sucesso" }
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Validação | `Validation failed` + `errors` (ex.: `"novaSenha": "A nova senha deve ter no mínimo 6 caracteres"`) |
| `400`  | Email não cadastrado **ou** senha atual errada (mesma mensagem, para não revelar quais emails existem) | `Senha atual incorreta` |

### `DELETE /usuario/deletar` — Deletar usuário autenticado

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Descrição** | Remove o usuário dono do token (*soft delete*: o registro permanece no banco, marcado como deletado, e o email é liberado para novo cadastro). |

**Resposta `200 OK`**

```json
{ "message": "Usuário deletado com sucesso" }
```

**Erros possíveis**: `401` (sem token / token inválido ou expirado).

---

## 3. Escola

Somente leitura. Escolas não têm cadastro pela API.

### `GET /escola` — Listar escolas

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `page`, `size`, `sort` — ordenação por `nome`, `tipo`, `endereco` |

**Resposta `200 OK`**

```json
{
  "content": [
    {
      "id": "3f2a7c1e-9b4d-4e8a-a1c2-5d6e7f8a9b0c",
      "nome": "Escola Estadual Seis de Maio",
      "tipo": "PUBLICA",
      "endereco": "Rua das Flores, 100"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "...": "..."
}
```

> No banco de demonstração não há escolas cadastradas, então a lista vem com `"content": []`.

### `GET /escola/{id}` — Buscar escola por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**: um objeto escola, como no item de `content` acima.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `404`  | ID inexistente | `Escola não encontrada com o ID: <id>` |

---

## 4. Turma

### `GET /turma` — Listar turmas

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `page`, `size`, `sort` — ordenação por `nome`, `turno`, `ensino` |
| **Descrição** | Lista paginada de turmas, com a contagem de alunos de cada uma. |

Exemplo: `GET /turma?size=2&sort=nome,asc`

**Resposta `200 OK`**

```json
{
  "content": [
    {
      "id": "6e37850a-0d53-479d-b5e5-09e1603d466b",
      "nome": "1º ano Médio",
      "turno": "VESPERTINO",
      "ensino": "MEDIO",
      "quantidadeAlunos": 8
    },
    {
      "id": "d2f6aa31-7a80-4be6-8b70-70d557bf4dab",
      "nome": "6º ano A",
      "turno": "DIURNO",
      "ensino": "FUNDAMENTAL",
      "quantidadeAlunos": 9
    }
  ],
  "number": 0,
  "size": 2,
  "totalElements": 9,
  "totalPages": 5,
  "...": "..."
}
```

### `GET /turma/{id}` — Buscar turma por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**

```json
{
  "id": "6e37850a-0d53-479d-b5e5-09e1603d466b",
  "nome": "1º ano Médio",
  "turno": "VESPERTINO",
  "ensino": "MEDIO",
  "quantidadeAlunos": 8
}
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | `id` não é UUID | `Valor inválido para o parâmetro 'id'` |
| `404`  | ID inexistente | `Turma não encontrada com o ID: <id>` |

### `POST /turma` — Criar turma

| | |
|---|---|
| **Autenticação** | Bearer token |

**Corpo da requisição**

| Campo    | Tipo   | Obrigatório | Regras |
| -------- | ------ | ----------- | ------ |
| `nome`   | string | sim         | não vazio, único |
| `turno`  | enum   | sim         | `Turno` |
| `ensino` | enum   | sim         | `Ensino` |

```json
{
  "nome": "7º ano B",
  "turno": "DIURNO",
  "ensino": "FUNDAMENTAL"
}
```

**Resposta `201 Created`**

```json
{
  "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
  "nome": "7º ano B",
  "turno": "DIURNO",
  "ensino": "FUNDAMENTAL",
  "quantidadeAlunos": 0
}
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Nome já usado por outra turma | `Já existe uma turma com este nome` |
| `400`  | Validação | `Validation failed` + `errors` (ex.: `"turno": "Turno é obrigatório"`) |
| `400`  | Valor de enum inválido | `Corpo da requisição inválido ou malformado` |

### `PUT /turma/{id}` — Atualizar turma

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Substitui nome, turno e ensino. |

**Corpo da requisição**: igual ao de `POST /turma` (todos os campos obrigatórios).

**Resposta `200 OK`**: a turma atualizada, no mesmo formato de `GET /turma/{id}`.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Nome já usado por outra turma | `Já existe uma turma com este nome` |
| `400`  | Validação | `Validation failed` + `errors` |
| `404`  | ID inexistente | `Turma não encontrada com o ID: <id>` |

### `DELETE /turma/{id}` — Deletar turma

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Remove a turma. Só é permitido se não houver alunos vinculados. |

**Resposta `200 OK`**

```json
{ "message": "Turma deletada com sucesso" }
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Há alunos na turma | `Não é possível excluir a turma: existem alunos vinculados a ela` |
| `404`  | ID inexistente | `Turma não encontrada com o ID: <id>` |

---

## 5. Aluno

### `GET /aluno` — Listar alunos

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `nome` (string, opcional), `turmaId` (UUID, opcional), `ativo` (boolean, opcional); `page`, `size`, `sort` — ordenação por `nome` |
| **Descrição** | Lista paginada de alunos (os excluídos não aparecem; os que saíram da escola aparecem com `ativo: false`). Filtros combináveis, cada um ignorado quando ausente. |

| Filtro    | Comportamento |
| --------- | ------------- |
| `nome`    | Trecho do nome do aluno **ou** do nome da turma, sem diferenciar maiúsculas nem acentos (`?nome=joao` encontra "João"; `?nome=6b` encontra os alunos do "6º B"). |
| `turmaId` | Só alunos da turma. |
| `ativo`   | `true`: só ativos; `false`: só os que saíram; ausente: todos. |

Exemplos: `GET /aluno?size=2&sort=nome,asc` · `GET /aluno?nome=beatriz&ativo=true` · `GET /aluno?turmaId=<uuid>&ativo=false`

**Resposta `200 OK`**

```json
{
  "content": [
    {
      "id": "1524a841-afed-47e1-a940-1c7b4c5337cf",
      "nome": "Beatriz Lima Mendes",
      "nomeResponsavel": null,
      "telefoneResponsavel": null,
      "turmaId": "b8280928-01d3-4af7-bf11-71961de0613a",
      "turmaNome": "8º ano A",
      "ativo": true,
      "dataSaida": null
    },
    {
      "id": "c77777ac-65ee-44ed-a8c8-04bc77c99927",
      "nome": "Caio Almeida Martins",
      "nomeResponsavel": "Carla Martins",
      "telefoneResponsavel": "11922647330",
      "turmaId": "6e37850a-0d53-479d-b5e5-09e1603d466b",
      "turmaNome": "1º ano Médio",
      "ativo": false,
      "dataSaida": "2026-11-10T14:05:12"
    }
  ],
  "totalElements": 54,
  "totalPages": 27,
  "...": "..."
}
```

### `GET /aluno/{id}` — Buscar aluno por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**: um objeto aluno, como no item de `content` acima.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `404`  | ID inexistente ou aluno excluído | `Aluno não encontrado com o ID: <id>` |

### `POST /aluno` — Criar aluno

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Descrição** | Cadastra um aluno vinculado a uma turma existente. |

**Corpo da requisição**

| Campo                 | Tipo   | Obrigatório | Regras |
| --------------------- | ------ | ----------- | ------ |
| `nome`                | string | sim         | não vazio |
| `turmaId`             | UUID   | sim         | turma existente |
| `nomeResponsavel`     | string | não         | até 255 caracteres |
| `telefoneResponsavel` | string | não         | só dígitos, com DDD: 10 ou 11 dígitos (ou vazio) |

```json
{
  "nome": "Caio Almeida Martins",
  "nomeResponsavel": "Carla Martins",
  "telefoneResponsavel": "11922647330",
  "turmaId": "6e37850a-0d53-479d-b5e5-09e1603d466b"
}
```

**Resposta `201 Created`**

```json
{
  "id": "c77777ac-65ee-44ed-a8c8-04bc77c99927",
  "nome": "Caio Almeida Martins",
  "nomeResponsavel": "Carla Martins",
  "telefoneResponsavel": "11922647330",
  "turmaId": "6e37850a-0d53-479d-b5e5-09e1603d466b",
  "turmaNome": "1º ano Médio",
  "ativo": true,
  "dataSaida": null
}
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Validação | `Validation failed` + `errors` (ex.: `"telefoneResponsavel": "Telefone deve conter apenas dígitos, com DDD (10 ou 11 dígitos)"`, `"turmaId": "Turma é obrigatória"`) |
| `404`  | Turma inexistente | `Turma não encontrada com o ID: <id>` |

### `PUT /aluno/{id}` — Atualizar aluno

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Atualiza nome, dados do responsável e/ou turma. |

**Corpo da requisição**: mesmos campos e regras de `POST /aluno`.

**Resposta `200 OK`**: o aluno atualizado.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Validação | `Validation failed` + `errors` |
| `404`  | Aluno inexistente | `Aluno não encontrado com o ID: <id>` |
| `404`  | Turma inexistente | `Turma não encontrada com o ID: <id>` |

### `PATCH /aluno/{id}/saida` — Registrar saída do aluno

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Desliga o aluno que deixou a escola: `ativo = false` e `dataSaida` = agora. O cadastro e o histórico de entregas são mantidos (continuam na listagem, no detalhe e nos relatórios), mas o aluno **não pode receber novas entregas**. Sem corpo. |

**Resposta `200 OK`**: o aluno com `"ativo": false` e `dataSaida` preenchida.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Saída já registrada | `A saída deste aluno já foi registrada` |
| `404`  | Aluno inexistente | `Aluno não encontrado com o ID: <id>` |

### `PATCH /aluno/{id}/reativar` — Reativar aluno

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Desfaz a saída (`ativo = true`, `dataSaida = null`); o aluno volta a poder receber entregas. Sem corpo. |

**Resposta `200 OK`**: o aluno reativado.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Aluno já ativo | `Este aluno já está ativo` |
| `404`  | Aluno inexistente | `Aluno não encontrado com o ID: <id>` |

### `DELETE /aluno/{id}` — Deletar aluno

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Remove um aluno **cadastrado por engano** (*soft delete*). É recusado quando o aluno já tem entregas: nesse caso use `PATCH /aluno/{id}/saida`, que preserva o histórico. |

**Resposta `200 OK`**

```json
{ "message": "Aluno deletado com sucesso" }
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Aluno tem entregas registradas | `Este aluno tem entregas registradas. Use 'Registrar saída' para manter o histórico.` |
| `404`  | Aluno inexistente | `Aluno não encontrado com o ID: <id>` |

---

## 6. Tipo de uniforme

Catálogo das peças (camiseta, bermuda, agasalho…). Cada tipo define o seu **limite de estoque baixo** (`estoqueBaixo`): quando uma combinação de tamanho e sexo desse tipo fica com quantidade menor ou igual ao limite, ela entra em alerta.

### `GET /tipo-uniforme` — Listar tipos de uniforme

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `page`, `size`, `sort` — ordenação por `tipo` |

**Resposta `200 OK`**

```json
{
  "content": [
    { "id": "932eeb45-c462-4731-8189-06156be1e899", "tipo": "Short-saia", "estoqueBaixo": 3 },
    { "id": "78ceae2e-8b2b-41ca-8713-c3d4d844384d", "tipo": "Calça de agasalho", "estoqueBaixo": 2 },
    { "id": "8be4221e-dc93-40ff-8581-fb5ef69499bd", "tipo": "Bermuda", "estoqueBaixo": 8 }
  ],
  "totalElements": 7,
  "...": "..."
}
```

### `GET /tipo-uniforme/{id}` — Buscar tipo de uniforme por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**

```json
{ "id": "932eeb45-c462-4731-8189-06156be1e899", "tipo": "Short-saia", "estoqueBaixo": 3 }
```

**Erros possíveis**: `404` — `Tipo de uniforme não encontrado com o ID: <id>`.

### `POST /tipo-uniforme` — Criar tipo de uniforme

| | |
|---|---|
| **Autenticação** | Bearer token |

**Corpo da requisição**

| Campo          | Tipo    | Obrigatório | Regras |
| -------------- | ------- | ----------- | ------ |
| `tipo`         | string  | sim         | não vazio, único |
| `estoqueBaixo` | inteiro | sim         | `>= 0` — avisar quando restar esta quantidade ou menos |

```json
{
  "tipo": "Camiseta manga longa",
  "estoqueBaixo": 5
}
```

**Resposta `201 Created`**

```json
{ "id": "5c0d6f2b-3e1a-4b7c-9d8e-1f2a3b4c5d6e", "tipo": "Camiseta manga longa", "estoqueBaixo": 5 }
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Nome já existe | `Já existe um tipo de uniforme com este nome` |
| `400`  | Validação | `Validation failed` + `errors` (ex.: `"estoqueBaixo": "Estoque baixo não pode ser negativo"`) |

### `PUT /tipo-uniforme/{id}` — Atualizar tipo de uniforme

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Corpo da requisição**: igual ao de `POST /tipo-uniforme`. Alterar `estoqueBaixo` muda imediatamente o `estoqueBaixoAlerta` de todos os uniformes desse tipo.

**Resposta `200 OK`**: o tipo atualizado.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Nome já existe em outro tipo | `Já existe um tipo de uniforme com este nome` |
| `400`  | Validação | `Validation failed` + `errors` |
| `404`  | ID inexistente | `Tipo de uniforme não encontrado com o ID: <id>` |

### `DELETE /tipo-uniforme/{id}` — Deletar tipo de uniforme

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Só é permitido se não houver uniformes em estoque nem itens de lote desse tipo. |

**Resposta `200 OK`**

```json
{ "message": "Tipo de uniforme deletado com sucesso" }
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Há uniformes do tipo | `Não é possível excluir o tipo de uniforme: existem uniformes vinculados a ele` |
| `400`  | Há itens de lote do tipo | `Não é possível excluir o tipo de uniforme: existem itens em lote vinculados a ele` |
| `404`  | ID inexistente | `Tipo de uniforme não encontrado com o ID: <id>` |

---

## 7. Uniforme (estoque)

Somente leitura. Cada registro é uma combinação **tipo + tamanho + sexo** com sua quantidade atual. Os uniformes são criados e incrementados automaticamente pelos lotes (entrada) e decrementados pelos pedidos (saída) — não há `POST`/`PUT` direto.

### `GET /uniforme` — Listar uniformes em estoque

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `page`, `size`, `sort` — ordenação por `tamanho`, `quantidade`, `sexo` |

Exemplo: `GET /uniforme?size=2&sort=quantidade,asc` (os mais críticos primeiro)

**Resposta `200 OK`**

```json
{
  "content": [
    {
      "id": "f32263c7-129b-417e-a9ad-d2052d06c5b2",
      "tipoUniformeId": "8be4221e-dc93-40ff-8581-fb5ef69499bd",
      "tipoUniformeNome": "Bermuda",
      "tamanho": "GG",
      "quantidade": 0,
      "sexo": "FEMININO",
      "estoqueBaixo": 8,
      "estoqueBaixoAlerta": true
    },
    {
      "id": "f4ef8425-f7ae-44b0-bd34-072af123f73f",
      "tipoUniformeId": "294d83dc-d343-4968-a339-a9acbfae60a8",
      "tipoUniformeNome": "Jaqueta de agasalho",
      "tamanho": "GG",
      "quantidade": 0,
      "sexo": "FEMININO",
      "estoqueBaixo": 3,
      "estoqueBaixoAlerta": true
    }
  ],
  "totalElements": 53,
  "totalPages": 27,
  "...": "..."
}
```

| Campo                | Significado |
| -------------------- | ----------- |
| `quantidade`         | Peças disponíveis agora. |
| `estoqueBaixo`       | Limite definido no tipo de uniforme. |
| `estoqueBaixoAlerta` | `true` quando `quantidade <= estoqueBaixo` (inclui o esgotado, `quantidade = 0`). |

**Erros possíveis**: `400` — `Parâmetro de ordenação inválido: <campo>`.

### `GET /uniforme/painel` — Painel de estoque

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros** | nenhum |
| **Descrição** | Lê a VIEW `vw_estoque` (ver [ERD](./ERD.md#view-vw_estoque-v4)): uma linha por combinação tipo + tamanho + sexo, já com a situação calculada. Não é paginado; ordenado por tipo, tamanho e sexo. Sem estoque, devolve `[]`. Usado pelo dashboard e pela tela de estoque. |

**Resposta `200 OK`**

```json
[
  {
    "uniformeId": "f32263c7-129b-417e-a9ad-d2052d06c5b2",
    "tipoUniformeId": "8be4221e-dc93-40ff-8581-fb5ef69499bd",
    "tipoUniformeNome": "Bermuda",
    "tamanho": "GG",
    "sexo": "FEMININO",
    "quantidade": 0,
    "estoqueBaixo": 8,
    "situacao": "ESGOTADO"
  }
]
```

| `situacao` | Regra |
| ---------- | ----- |
| `ESGOTADO` | `quantidade <= 0` |
| `BAIXO`    | `quantidade <= estoqueBaixo` (o limite é do **tipo de uniforme**; o próprio limite já conta como baixo) |
| `OK`       | acima do limite |

### `GET /uniforme/{id}` — Buscar uniforme por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**

```json
{
  "id": "73007603-3b5e-4d17-b802-4b74d485113b",
  "tipoUniformeId": "294d83dc-d343-4968-a339-a9acbfae60a8",
  "tipoUniformeNome": "Jaqueta de agasalho",
  "tamanho": "P",
  "quantidade": 4,
  "sexo": "MASCULINO",
  "estoqueBaixo": 3,
  "estoqueBaixoAlerta": false
}
```

**Erros possíveis**: `404` — `Uniforme não encontrado com o ID: <id>`.

---

## 8. Lote (entrada de estoque)

Um lote é uma entrega do fornecedor, com a nota fiscal e os itens recebidos. **Criar um lote faz o estoque subir**: para cada item, a combinação tipo + tamanho + sexo é incrementada (e criada, se ainda não existir). Tudo acontece numa única transação.

### `GET /lote` — Listar lotes

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `page`, `size`, `sort` — ordenação por `fornecedor`, `dataEntrega` |

Exemplo: `GET /lote?size=1&sort=dataEntrega,desc`

**Resposta `200 OK`**

```json
{
  "content": [
    {
      "id": "ca43c0eb-621c-4f75-9e93-48b2cf2b1e2a",
      "notaFiscalId": "6d1a5e7f-bef2-4d00-9e71-5bdfd1e105d7",
      "notaFiscalChaveAcesso": "35261012345678000190550010000012341000012345",
      "fornecedor": "Têxtil Paraná",
      "dataEntrega": "2026-10-18T00:00:00",
      "itens": [
        {
          "id": "d5997125-8e54-464a-b6bd-fa941a1fbf42",
          "tipoUniformeId": "932eeb45-c462-4731-8189-06156be1e899",
          "tipoUniformeNome": "Short-saia",
          "loteId": "ca43c0eb-621c-4f75-9e93-48b2cf2b1e2a",
          "loteFornecedor": "Têxtil Paraná",
          "tamanho": "M",
          "quantidade": 12,
          "sexo": "FEMININO"
        },
        {
          "id": "bf14207d-b277-4065-8116-9c987ccc143e",
          "tipoUniformeId": "932eeb45-c462-4731-8189-06156be1e899",
          "tipoUniformeNome": "Short-saia",
          "loteId": "ca43c0eb-621c-4f75-9e93-48b2cf2b1e2a",
          "loteFornecedor": "Têxtil Paraná",
          "tamanho": "G",
          "quantidade": 6,
          "sexo": "FEMININO"
        }
      ]
    }
  ],
  "totalElements": 8,
  "totalPages": 8,
  "...": "..."
}
```

### `GET /lote/{id}` — Buscar lote por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**: um objeto lote com seus itens, como no item de `content` acima.

**Erros possíveis**: `404` — `Lote não encontrado com o ID: <id>`.

### `POST /lote` — Criar lote (dar entrada no estoque)

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Descrição** | Registra a entrega do fornecedor: cria a nota fiscal com a chave de acesso informada, o lote e seus itens, e incrementa o estoque de cada item. |

**Corpo da requisição**

| Campo         | Tipo     | Obrigatório | Regras |
| ------------- | -------- | ----------- | ------ |
| `chaveAcesso` | string   | sim         | chave da NF-e, não vazia, **única** |
| `fornecedor`  | string   | sim         | não vazio |
| `dataEntrega` | datetime | não         | ISO 8601 sem fuso. Se omitida, vale o momento do registro (a coluna é `NOT NULL DEFAULT now()`); numa edição sem data, a data original é mantida. |
| `itens`       | array    | sim         | ao menos 1 item |
| `itens[].tipoUniformeId` | UUID | sim | tipo existente |
| `itens[].tamanho`        | enum | sim | `Tamanho` |
| `itens[].sexo`           | enum | sim | `Sexo` |
| `itens[].quantidade`     | inteiro | sim | `>= 1` |

Itens repetidos (mesmo tipo, tamanho e sexo) são somados num único item.

```json
{
  "chaveAcesso": "35261012345678000190550010000012341000012345",
  "fornecedor": "Têxtil Paraná",
  "dataEntrega": "2026-10-18T00:00:00",
  "itens": [
    { "tipoUniformeId": "932eeb45-c462-4731-8189-06156be1e899", "tamanho": "M", "sexo": "FEMININO", "quantidade": 12 },
    { "tipoUniformeId": "932eeb45-c462-4731-8189-06156be1e899", "tamanho": "G", "sexo": "FEMININO", "quantidade": 6 }
  ]
}
```

**Resposta `201 Created`**: o lote criado, no mesmo formato de `GET /lote/{id}`.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Chave de acesso já usada | `Já existe uma nota fiscal com esta chave de acesso` |
| `400`  | Validação | `Validation failed` + `errors` (ex.: `"itens": "O lote precisa ter ao menos um item"`, `"chaveAcesso": "Chave de acesso é obrigatória"`) |
| `400`  | Soma de quantidades estoura o limite de inteiro | `Quantidade total do item excede o limite permitido` |
| `404`  | Tipo de uniforme inexistente | `Tipo de uniforme não encontrado com o ID: <id>` |
| `409`  | Outra operação criando a mesma combinação ao mesmo tempo | `Este uniforme está sendo registrado por outra operação neste momento. Tente novamente` |

### `PUT /lote/{id}` — Atualizar lote

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Corrige um lote já lançado. Os itens são **substituídos**: a API dá entrada nos itens novos e estorna do estoque os itens antigos, na mesma transação (as linhas de estoque envolvidas são travadas antes, sempre na mesma ordem, para evitar deadlock). |

**Corpo da requisição**: mesmos campos e regras de `POST /lote`.

**Resposta `200 OK`**: o lote atualizado com os novos itens.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Estornar os itens antigos deixaria o estoque negativo (parte das peças já foi entregue) | `Não é possível estornar a entrada: o estoque ficaria negativo, pois parte das unidades já saiu em pedidos` |
| `400`  | Chave de acesso já usada por outra nota | `Já existe uma nota fiscal com esta chave de acesso` |
| `400`  | Validação | `Validation failed` + `errors` |
| `404`  | Lote inexistente | `Lote não encontrado com o ID: <id>` |
| `404`  | Tipo de uniforme inexistente | `Tipo de uniforme não encontrado com o ID: <id>` |

> Não há `DELETE /lote`: uma entrada lançada é corrigida com `PUT`, preservando o histórico para a prestação de contas.

---

## 9. Item de lote

Somente leitura. Cada linha recebida em um lote. Útil para consultas e conferência; os itens são criados via `POST /lote`.

### `GET /item-lote` — Listar itens de lote

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `page`, `size`, `sort` — ordenação por `tamanho`, `quantidade`, `sexo` |

**Resposta `200 OK`**

```json
{
  "content": [
    {
      "id": "4a5fff73-0830-4297-ae70-74af79386b06",
      "tipoUniformeId": "d95c079c-8b47-429c-a8c5-085420363bad",
      "tipoUniformeNome": "Camiseta manga curta",
      "loteId": "5d263a2c-a122-4885-95a2-de8074f10231",
      "loteFornecedor": "Confecções Aurora",
      "tamanho": "PP",
      "quantidade": 6,
      "sexo": "FEMININO"
    }
  ],
  "totalElements": 63,
  "...": "..."
}
```

**Erros possíveis**: `400` — `Parâmetro de ordenação inválido: <campo>`.

### `GET /item-lote/{id}` — Buscar item de lote por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**: um objeto item de lote, como acima.

**Erros possíveis**: `404` — `Item de lote não encontrado com o ID: <id>`.

---

## 10. Nota fiscal

Somente leitura. A nota fiscal é criada junto com o lote (`POST /lote`), a partir da chave de acesso.

### `GET /nota-fiscal` — Listar notas fiscais

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `page`, `size`, `sort` — ordenação por `chaveAcesso` |

**Resposta `200 OK`**

```json
{
  "content": [
    { "id": "5ed7cd89-1583-41a0-9c22-80667cb1cdd4", "chaveAcesso": "71278880696587747245214176220893411824537229" },
    { "id": "b59777fe-1898-4137-9c02-57b6102508c3", "chaveAcesso": "08133984074806072286081271397457105692385299" }
  ],
  "totalElements": 8,
  "...": "..."
}
```

### `GET /nota-fiscal/{id}` — Buscar nota fiscal por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**

```json
{ "id": "5ed7cd89-1583-41a0-9c22-80667cb1cdd4", "chaveAcesso": "71278880696587747245214176220893411824537229" }
```

**Erros possíveis**: `404` — `Nota fiscal não encontrada com o ID: <id>`.

---

## 11. Pedido (entrega ao aluno)

Um pedido registra a entrega de uma ou mais peças a um aluno. **Criar um pedido faz o estoque descer.** O usuário responsável é o dono do token.

- `alunoAtivo` indica se o aluno ainda está na escola (a tela de entregas mostra o selo "Saiu").
- Se o cadastro do aluno ou do usuário não puder ser carregado (registros antigos excluídos antes da saída existir), a entrega continua listada com `alunoNome: "Aluno removido"` / `usuarioNome: "Usuário removido"`, em vez de derrubar a listagem.
- Itens com o mesmo `uniformeId` são somados; as linhas de estoque são travadas antes, sempre na mesma ordem (sem deadlock entre entregas simultâneas).

### `GET /pedido` — Listar pedidos

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `alunoId` (UUID, opcional) — filtra os pedidos de um aluno; `page`, `size`, `sort` — ordenação por `dataEfetivada` |

Exemplos: `GET /pedido?size=1&sort=dataEfetivada,desc` · `GET /pedido?alunoId=27cf31c7-00b6-49f6-9a94-070530f34665`

**Resposta `200 OK`**

```json
{
  "content": [
    {
      "id": "ed082820-0d59-4dad-8320-7eeb969f8e70",
      "alunoId": "27cf31c7-00b6-49f6-9a94-070530f34665",
      "alunoNome": "Laura Prado Vieira",
      "alunoAtivo": true,
      "usuarioId": "fb804bdf-c281-4909-b5be-b755eeefdfe9",
      "usuarioNome": "Marina Lopes",
      "dataEfetivada": "2026-10-18T21:10:50",
      "itens": [
        {
          "id": "19c2855d-05c9-4c7c-b326-382f2e24bcb5",
          "pedidoId": "ed082820-0d59-4dad-8320-7eeb969f8e70",
          "uniformeId": "e38489d4-9699-43d1-82d3-1132d050b8ae",
          "uniformeTipoUniformeNome": "Camiseta manga curta",
          "uniformeTamanho": "M",
          "uniformeSexo": "FEMININO",
          "quantidade": 3
        },
        {
          "id": "77f662f7-af27-46ff-b9c7-0d17464f3e8d",
          "pedidoId": "ed082820-0d59-4dad-8320-7eeb969f8e70",
          "uniformeId": "04bfc0ef-65a4-4508-97b6-c1998f190f49",
          "uniformeTipoUniformeNome": "Boné 6961",
          "uniformeTamanho": "M",
          "uniformeSexo": "FEMININO",
          "quantidade": 1
        }
      ]
    }
  ],
  "totalElements": 43,
  "totalPages": 43,
  "...": "..."
}
```

**Erros possíveis**: `400` — `Parâmetro de ordenação inválido: <campo>` ou `Valor inválido para o parâmetro 'alunoId'`.

### `GET /pedido/{id}` — Buscar pedido por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**: um objeto pedido com seus itens, como no item de `content` acima.

**Erros possíveis**: `404` — `Pedido não encontrado com o ID: <id>`.

### `POST /pedido` — Criar pedido (registrar entrega)

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Descrição** | Registra a entrega e decrementa o estoque de cada uniforme. Se qualquer item não tiver saldo suficiente, nada é gravado (transação única). |

**Corpo da requisição**

| Campo                | Tipo     | Obrigatório | Regras |
| -------------------- | -------- | ----------- | ------ |
| `alunoId`            | UUID     | sim         | aluno existente e ativo |
| `dataEfetivada`      | datetime | não         | ISO 8601 sem fuso. Se omitida, vale o momento do registro (a coluna é `NOT NULL DEFAULT now()`); numa edição sem data, a data original é mantida. |
| `itens`              | array    | sim         | ao menos 1 item |
| `itens[].uniformeId` | UUID     | sim         | uniforme existente (combinação tipo + tamanho + sexo) |
| `itens[].quantidade` | inteiro  | sim         | `>= 1` e `<=` saldo disponível |

Itens com o mesmo `uniformeId` são somados num único item.

```json
{
  "alunoId": "27cf31c7-00b6-49f6-9a94-070530f34665",
  "dataEfetivada": "2026-10-18T21:10:50",
  "itens": [
    { "uniformeId": "e38489d4-9699-43d1-82d3-1132d050b8ae", "quantidade": 3 },
    { "uniformeId": "04bfc0ef-65a4-4508-97b6-c1998f190f49", "quantidade": 1 }
  ]
}
```

**Resposta `201 Created`**: o pedido criado, no mesmo formato de `GET /pedido/{id}`, com `usuarioId`/`usuarioNome` do dono do token.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Quantidade maior que o saldo | `Quantidade solicitada (5) maior que o estoque disponível (2)` |
| `400`  | Aluno saiu da escola | `Este aluno saiu da escola em 10/11/2026 e não pode receber novas entregas. Reative o cadastro para registrar a entrega.` |
| `400`  | Validação | `Validation failed` + `errors` (ex.: `"itens": "O pedido precisa ter ao menos um item"`, `"alunoId": "Aluno é obrigatório"`) |
| `400`  | Soma de quantidades estoura o limite de inteiro | `Quantidade total do item excede o limite permitido` |
| `404`  | Aluno inexistente | `Aluno não encontrado com o ID: <id>` |
| `404`  | Uniforme inexistente | `Uniforme não encontrado com o ID: <id>` |

### `PUT /pedido/{id}` — Atualizar pedido

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | Substitui os itens do pedido: devolve ao estoque as quantidades antigas e decrementa as novas. O usuário que editou passa a constar no pedido. |

**Corpo da requisição**: mesmos campos e regras de `POST /pedido`.

**Resposta `200 OK`**: o pedido atualizado.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Quantidade nova maior que o saldo (já considerando a devolução dos itens antigos) | `Quantidade solicitada (...) maior que o estoque disponível (...)` |
| `400`  | Validação | `Validation failed` + `errors` |
| `400`  | Trocar a entrega para um aluno que saiu da escola (corrigir a entrega antiga de quem já saiu é permitido) | `Este aluno saiu da escola em ... e não pode receber novas entregas. ...` |
| `404`  | Pedido, aluno ou uniforme inexistente | `Pedido não encontrado com o ID: <id>` (ou de aluno/uniforme) |

> Não há `DELETE /pedido`: as entregas são o registro de prestação de contas.

---

## 12. Item de pedido

Somente leitura. Cada linha entregue em um pedido.

### `GET /pedido-uniforme` — Listar itens de pedido

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `page`, `size`, `sort` — ordenação por `quantidade` |

**Resposta `200 OK`**

```json
{
  "content": [
    {
      "id": "7dbc92be-6709-4c03-ba87-8112f5fc42d2",
      "pedidoId": "062c4f2f-9abb-4ba1-89fc-832b126a10f0",
      "uniformeId": "f32263c7-129b-417e-a9ad-d2052d06c5b2",
      "uniformeTipoUniformeNome": "Bermuda",
      "uniformeTamanho": "GG",
      "uniformeSexo": "FEMININO",
      "quantidade": 1
    }
  ],
  "totalElements": 78,
  "...": "..."
}
```

**Erros possíveis**: `400` — `Parâmetro de ordenação inválido: <campo>`.

### `GET /pedido-uniforme/{id}` — Buscar item de pedido por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Resposta `200 OK`**: um objeto item de pedido, como acima.

**Erros possíveis**: `404` — `Item do pedido não encontrado com o ID: <id>`.

---

## 13. Relatórios em PDF ou CSV

Todos os relatórios aceitam `formato=pdf` (padrão) ou `formato=csv` e respondem `200 OK` com o arquivo:

| `formato` | Content-Type | Arquivo |
| --------- | ------------ | ------- |
| `pdf` (ou ausente) | `application/pdf` | `relatorio-<nome>.pdf` |
| `csv` | `text/csv;charset=UTF-8` (UTF-8 com BOM, separador `;`, abre direto no Excel) | `relatorio-<nome>.csv` |

```
Content-Disposition: attachment; filename=relatorio-<nome>.<pdf|csv>
```

- **Período sem dados não é erro**: o arquivo sai com o cabeçalho e a indicação `Nenhum registro encontrado` (antes a API respondia `404`).
- `formato` inválido → `400` — `Formato de relatório inválido: use pdf ou csv`.
- O relatório de estoque lê a VIEW `vw_estoque` (colunas Tipo, Tamanho, Sexo, Quantidade, Limite, Situação).

### Filtro de período

Os relatórios de entrada, saída, entregas por turma e transações recebem o período por query string:

| Parâmetro   | Tipo    | Obrigatório | Regras |
| ----------- | ------- | ----------- | ------ |
| `tipo`      | enum    | sim         | `MES` ou `ANO` |
| `anoInicio` | inteiro | sim         | `>= 2000` |
| `mesInicio` | inteiro | quando `tipo=MES` | 1 a 12 |
| `anoFim`    | inteiro | não         | `>= 2000`; padrão = `anoInicio` |
| `mesFim`    | inteiro | não         | 1 a 12; padrão = `mesInicio` |

Como o período é montado:

- `tipo=MES`: do dia 1 de `mesInicio/anoInicio` 00:00:00 até o último dia de `mesFim/anoFim` 23:59:59.
- `tipo=ANO`: de 1º de janeiro de `anoInicio` até 31 de dezembro de `anoFim`.

Exemplos:

| Pedido | Query |
| ------ | ----- |
| Outubro de 2026 | `?tipo=MES&anoInicio=2026&mesInicio=10` |
| Agosto a novembro de 2026 | `?tipo=MES&anoInicio=2026&mesInicio=8&mesFim=11` |
| Ano de 2026 | `?tipo=ANO&anoInicio=2026` |
| 2025 e 2026 | `?tipo=ANO&anoInicio=2025&anoFim=2026` |

Erros do filtro (comuns aos quatro relatórios com período; nenhum relatório devolve `404` por falta de dados):

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | `tipo` ausente | `{"message":"Validation failed","errors":{"tipo":"Tipo de filtro é obrigatório"}}` |
| `400`  | `tipo=MES` sem `mesInicio` | `Mês de início é obrigatório quando o filtro é por mês` |
| `400`  | Início depois do fim | `O período de início não pode ser posterior ao período de fim` |
| `400`  | Ano < 2000 ou mês fora de 1–12 | `Ano inválido` / `Mês deve ser entre 1 e 12` |

### `GET /relatorio/estoque` — Relatório de estoque

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros** | `formato` — sem filtro de período, sempre reflete o estoque atual |
| **Descrição** | Situação atual de cada combinação tipo + tamanho + sexo (`ESGOTADO`/`BAIXO`/`OK`, da VIEW `vw_estoque`), com o total de peças. |
| **Arquivo** | `relatorio-estoque.pdf` / `.csv` |

**Erros possíveis**: `400` — formato inválido.

### `GET /relatorio/entrada` — Relatório de entrada

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | [filtro de período](#filtro-de-período) |
| **Descrição** | Peças recebidas dos fornecedores (lotes) no período. |
| **Arquivo** | `relatorio-entrada.pdf` / `.csv` |

**Erros possíveis**: os do filtro e formato inválido.

### `GET /relatorio/saida` — Relatório de saída

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | [filtro de período](#filtro-de-período) |
| **Descrição** | Uniformes entregues aos alunos (pedidos) no período. |
| **Arquivo** | `relatorio-saida.pdf` / `.csv` |

**Erros possíveis**: os do filtro e formato inválido.

### `GET /relatorio/entregas-turma` — Relatório de entregas por turma

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | [filtro de período](#filtro-de-período) + `turmaId` (UUID, opcional) |
| **Descrição** | Uniformes entregues no período agrupados por turma, com subtotal de cada uma. Com `turmaId`, restringe a uma turma; sem ele, cobre todas. |
| **Arquivo** | `relatorio-entregas-turma.pdf` / `.csv` |

Exemplo:

```bash
curl -s -o entregas.pdf -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/relatorio/entregas-turma?tipo=MES&anoInicio=2026&mesInicio=10"

# mesmo relatório em CSV
curl -s -o entregas.csv -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/relatorio/entregas-turma?tipo=MES&anoInicio=2026&mesInicio=10&formato=csv"
```

**Erros possíveis**: os do filtro, formato inválido e `400` — `Valor inválido para o parâmetro 'turmaId'`.

### `GET /relatorio/transacoes` — Relatório de transações

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | [filtro de período](#filtro-de-período) |
| **Descrição** | Entradas e saídas juntas no período, com total de entradas, total de saídas e saldo. |
| **Arquivo** | `relatorio-transacoes.pdf` / `.csv` |

**Erros possíveis**: os do filtro e formato inválido.

---

## 14. Requisição de novas peças

Registro do que precisa ser comprado/pedido ao fornecedor (ex.: a peça esgotou). **Não altera o estoque**: as peças só entram quando o lote é registrado (`POST /lote`). O solicitante é o dono do token. Ciclo de vida: `PENDENTE` → `ATENDIDA` ou `CANCELADA`; só requisições pendentes mudam de status.

### `POST /requisicao` — Criar requisição

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Descrição** | Cria a requisição com `status: PENDENTE` e `dataCriacao` = agora. |

**Corpo da requisição**

| Campo            | Tipo    | Obrigatório | Regras |
| ---------------- | ------- | ----------- | ------ |
| `tipoUniformeId` | UUID    | sim         | tipo existente |
| `tamanho`        | enum    | sim         | `Tamanho` |
| `sexo`           | enum    | sim         | `Sexo` |
| `quantidade`     | inteiro | sim         | `>= 1` |
| `observacao`     | string  | não         | até 500 caracteres; em branco vira `null` |

```json
{
  "tipoUniformeId": "8be4221e-dc93-40ff-8581-fb5ef69499bd",
  "tamanho": "GG",
  "sexo": "FEMININO",
  "quantidade": 20,
  "observacao": "Esgotou na primeira semana de aula"
}
```

**Resposta `201 Created`**

```json
{
  "id": "5d1f3a0e-6a8e-4c38-9d0c-2b8f4a7e91c3",
  "tipoUniformeId": "8be4221e-dc93-40ff-8581-fb5ef69499bd",
  "tipoUniforme": "Bermuda",
  "tamanho": "GG",
  "sexo": "FEMININO",
  "quantidade": 20,
  "observacao": "Esgotou na primeira semana de aula",
  "status": "PENDENTE",
  "usuarioId": "fb804bdf-c281-4909-b5be-b755eeefdfe9",
  "usuarioNome": "Marina Lopes",
  "dataCriacao": "2026-11-10T14:20:00",
  "dataConclusao": null
}
```

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Validação | `Validation failed` + `errors` (ex.: `"quantidade": "Quantidade deve ser no mínimo 1"`, `"tipoUniformeId": "Escolha o tipo de uniforme"`) |
| `404`  | Tipo de uniforme inexistente | `Tipo de uniforme não encontrado com o ID: <id>` |

### `GET /requisicao` — Listar requisições

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de query** | `status` (opcional, pode repetir: `?status=ATENDIDA&status=CANCELADA`); `page`, `size`, `sort` — ordenação por `dataCriacao`, `dataConclusao`, `quantidade` |
| **Descrição** | Lista paginada. Sem `status`, traz todas. A tela usa `?status=PENDENTE` para "em andamento" e `ATENDIDA`+`CANCELADA` para a aba "Atendidas e canceladas". |

Exemplo: `GET /requisicao?status=PENDENTE&sort=dataCriacao,desc`

**Resposta `200 OK`**: página (`content`, `totalElements`…) de objetos como o de `POST /requisicao`.

**Erros possíveis**: `400` — status inválido ou `Parâmetro de ordenação inválido: <campo>`.

### `GET /requisicao/{id}` — Buscar requisição por ID

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |

**Erros possíveis**: `404` — `Requisição não encontrada com o ID: <id>`.

### `PATCH /requisicao/{id}/atender` — Marcar como atendida

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | `status = ATENDIDA` e `dataConclusao` = agora. Sem corpo. **Não mexe no estoque** (a entrada é registrada pelo lote). |

**Resposta `200 OK`**: a requisição atualizada.

**Erros possíveis**

| Status | Quando | Mensagem |
| ------ | ------ | -------- |
| `400`  | Requisição não está pendente | `Só é possível atender uma requisição pendente. Esta requisição já está <status>` |
| `404`  | ID inexistente | `Requisição não encontrada com o ID: <id>` |

### `PATCH /requisicao/{id}/cancelar` — Cancelar

| | |
|---|---|
| **Autenticação** | Bearer token |
| **Parâmetros de path** | `id` (UUID) |
| **Descrição** | `status = CANCELADA` e `dataConclusao` = agora. Sem corpo. |

**Erros possíveis**: `400` — `Só é possível cancelar uma requisição pendente. ...`; `404` — requisição inexistente.

---

## Resumo de todos os endpoints

| Método   | Rota                          | Auth    | Descrição |
| -------- | ----------------------------- | ------- | --------- |
| `POST`   | `/auth/login`                 | Pública | Autenticar e obter token |
| `POST`   | `/usuario/registrar`          | Pública | Registrar usuário |
| `PUT`    | `/usuario/trocar-senha`       | Pública | Trocar senha (email + senha atual) |
| `PUT`    | `/usuario/atualizar`          | Token   | Atualizar nome/email do usuário logado |
| `DELETE` | `/usuario/deletar`            | Token   | Excluir (soft delete) o usuário logado |
| `GET`    | `/escola`                     | Token   | Listar escolas |
| `GET`    | `/escola/{id}`                | Token   | Buscar escola |
| `GET`    | `/turma`                      | Token   | Listar turmas |
| `GET`    | `/turma/{id}`                 | Token   | Buscar turma |
| `POST`   | `/turma`                      | Token   | Criar turma |
| `PUT`    | `/turma/{id}`                 | Token   | Atualizar turma |
| `DELETE` | `/turma/{id}`                 | Token   | Excluir turma (sem alunos) |
| `GET`    | `/aluno`                      | Token   | Listar alunos (filtros `nome`, `turmaId`, `ativo`) |
| `GET`    | `/aluno/{id}`                 | Token   | Buscar aluno |
| `POST`   | `/aluno`                      | Token   | Criar aluno |
| `PUT`    | `/aluno/{id}`                 | Token   | Atualizar aluno |
| `PATCH`  | `/aluno/{id}/saida`           | Token   | Registrar saída do aluno (desligamento) |
| `PATCH`  | `/aluno/{id}/reativar`        | Token   | Reativar aluno |
| `DELETE` | `/aluno/{id}`                 | Token   | Excluir aluno cadastrado por engano (recusa se tiver entregas) |
| `GET`    | `/tipo-uniforme`              | Token   | Listar tipos de uniforme |
| `GET`    | `/tipo-uniforme/{id}`         | Token   | Buscar tipo de uniforme |
| `POST`   | `/tipo-uniforme`              | Token   | Criar tipo de uniforme |
| `PUT`    | `/tipo-uniforme/{id}`         | Token   | Atualizar tipo de uniforme |
| `DELETE` | `/tipo-uniforme/{id}`         | Token   | Excluir tipo de uniforme (sem vínculos) |
| `GET`    | `/uniforme`                   | Token   | Listar estoque |
| `GET`    | `/uniforme/painel`            | Token   | Painel de estoque com situação (VIEW `vw_estoque`) |
| `GET`    | `/uniforme/{id}`              | Token   | Buscar item de estoque |
| `GET`    | `/lote`                       | Token   | Listar lotes |
| `GET`    | `/lote/{id}`                  | Token   | Buscar lote |
| `POST`   | `/lote`                       | Token   | Criar lote (entrada; estoque sobe) |
| `PUT`    | `/lote/{id}`                  | Token   | Corrigir lote (substitui itens) |
| `GET`    | `/item-lote`                  | Token   | Listar itens de lote |
| `GET`    | `/item-lote/{id}`             | Token   | Buscar item de lote |
| `GET`    | `/nota-fiscal`                | Token   | Listar notas fiscais |
| `GET`    | `/nota-fiscal/{id}`           | Token   | Buscar nota fiscal |
| `GET`    | `/pedido`                     | Token   | Listar pedidos (filtro `alunoId`) |
| `GET`    | `/pedido/{id}`                | Token   | Buscar pedido |
| `POST`   | `/pedido`                     | Token   | Criar pedido (entrega; estoque desce) |
| `PUT`    | `/pedido/{id}`                | Token   | Corrigir pedido (substitui itens) |
| `GET`    | `/pedido-uniforme`            | Token   | Listar itens de pedido |
| `GET`    | `/pedido-uniforme/{id}`       | Token   | Buscar item de pedido |
| `GET`    | `/relatorio/estoque`          | Token   | PDF/CSV do estoque atual |
| `GET`    | `/relatorio/entrada`          | Token   | PDF/CSV de entradas no período |
| `GET`    | `/relatorio/saida`            | Token   | PDF/CSV de saídas no período |
| `GET`    | `/relatorio/entregas-turma`   | Token   | PDF/CSV de entregas por turma no período |
| `GET`    | `/relatorio/transacoes`       | Token   | PDF/CSV de entradas + saídas + saldo no período |
| `POST`   | `/requisicao`                 | Token   | Criar requisição de novas peças |
| `GET`    | `/requisicao`                 | Token   | Listar requisições (filtro `status`) |
| `GET`    | `/requisicao/{id}`            | Token   | Buscar requisição |
| `PATCH`  | `/requisicao/{id}/atender`    | Token   | Marcar requisição como atendida |
| `PATCH`  | `/requisicao/{id}/cancelar`   | Token   | Cancelar requisição |

Toda rota "Token" responde `401` (JSON) sem token ou com token inválido/expirado. Relatórios aceitam `?formato=pdf|csv`.
