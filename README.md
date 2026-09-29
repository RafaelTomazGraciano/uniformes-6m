# Uniformes 6M

Sistema de gestão de uniformes escolares: registra a entrada dos lotes do fornecedor, a entrega dos uniformes aos alunos, acompanha o saldo de cada tamanho e emite os relatórios em PDF para prestação de contas.

O repositório tem duas partes independentes:

| Parte                     | Stack                                        | Documentação                          |
| ------------------------- | -------------------------------------------- | ------------------------------------- |
| [`backend/`](./backend)   | Java 25, Spring Boot, PostgreSQL, Docker      | [BACKEND.md](./backend/BACKEND.md)    |
| [`frontend/`](./frontend) | React 19, TypeScript, Vite, Tailwind          | [README](./frontend/README.md)        |

## Rodando o projeto

A ordem importa: sem a API no ar, a interface abre mas todas as telas ficam vazias.

**1. Backend** — precisa de Docker e de um `.env` (veja `backend/.env.example`):

```bash
cd backend
docker compose up -d --build
```

A API sobe em `http://localhost:8080`, com o Swagger em `http://localhost:8080/swagger-ui.html`.

**2. Frontend** — precisa de Node 20+ e pnpm:

```bash
cd frontend
pnpm install
cp .env.example .env
pnpm dev
```

A interface sobe em `http://localhost:5173`. Não há usuário cadastrado na primeira execução: crie o seu em `/register`.

As instruções completas de cada parte — variáveis de ambiente, scripts, testes e estrutura — estão nos READMEs linkados na tabela acima.

## Modelagem

O diagrama entidade-relacionamento do banco está em [`backend/DER.png`](./backend/DER.png), com o detalhamento das tabelas na [seção de banco de dados do BACKEND.md](./backend/BACKEND.md#banco-de-dados).

## Testes

```bash
cd backend && ./gradlew test    # JUnit 5, Mockito e Testcontainers
cd frontend && pnpm test        # Vitest e Testing Library
```
