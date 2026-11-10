# 6M Uniformes — Frontend

Interface web do sistema de gestão de uniformes escolares. Cobre o ciclo completo do almoxarifado: a entrada dos lotes do fornecedor, a entrega dos uniformes aos alunos, o acompanhamento do estoque por tamanho, a requisição de reposição, a saída de alunos e a emissão dos relatórios em PDF ou CSV.

Consome a API documentada em [BACKEND.md](../backend/BACKEND.md) e em [docs/endpoints.md](../docs/endpoints.md).

## Sumário

- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Como rodar o projeto](#como-rodar-o-projeto)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Scripts disponíveis](#scripts-disponíveis)
- [Telas](#telas)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Testes](#testes)
- [Padrões usados no código](#padrões-usados-no-código)
- [Outros READMEs do projeto](#outros-readmes-do-projeto)

---

## Tecnologias

- **React 19** + **TypeScript**
- **Vite** (dev server e build)
- **Tailwind CSS 4** + **shadcn/ui** (Base UI e Radix por baixo)
- **TanStack Query** (cache e sincronização com a API)
- **React Router** (rotas e proteção de sessão)
- **React Hook Form** + **Zod** (formulários e validação)
- **Sonner** (toasts de sucesso e erro)
- **Vitest** + **Testing Library** (testes)
- **Oxlint** (lint)

## Pré-requisitos

- **Node.js 20+** (desenvolvido com a 24)
- **pnpm** (o repositório versiona um `pnpm-lock.yaml`). Sem pnpm instalado, use `npx -y pnpm@10 <comando>` (ex.: `npx -y pnpm@10 install`).
- O **backend rodando** — sem ele a interface abre, mas toda tela fica vazia. Veja [como subir a API](../backend/BACKEND.md#como-rodar-o-projeto).

## Como rodar o projeto

```bash
cd frontend
pnpm install
cp .env.example .env
pnpm dev
```

A aplicação sobe em `http://localhost:5173`.
Com o `host` do Vite configurado como `0.0.0.0`, também pode ser acessada na rede local em `http://IP_DA_MAQUINA:5173`.

Com os dados de demonstração (`node backend/scripts/seed.mjs`, a partir da raiz do repositório), entre com **admin@uniformes6m.local / senha123**. Sem o seed não há usuário: crie o seu em `/register`.

### Build de produção

```bash
pnpm build     # roda o typecheck (tsc -b) e gera o dist/
pnpm preview   # serve o dist/ localmente para conferência
```

## Variáveis de ambiente

| Variável       | Obrigatória | Padrão                      | Para que serve                        |
| -------------- | ----------- | --------------------------- | ------------------------------------- |
| `VITE_API_URL` | Não         | `http://localhost:8080/api` | URL base da API que o front consome. |

O padrão já aponta para o backend local, então o `.env` só é necessário quando a API estiver em outro endereço.
Para acesso em LAN, configure `VITE_API_URL` com o IP da máquina que está rodando o backend (ex.: `http://192.168.1.10:8080/api`).

> As variáveis do Vite são lidas no momento do build. Depois de alterar o `.env`, reinicie o `pnpm dev`.

## Scripts disponíveis

| Comando          | O que faz                                        |
| ---------------- | ------------------------------------------------ |
| `pnpm dev`       | Sobe o dev server com HMR.                        |
| `pnpm build`     | Typecheck com `tsc -b` e build de produção.       |
| `pnpm preview`   | Serve o build gerado.                             |
| `pnpm test`      | Roda a suíte de testes uma vez.                   |
| `pnpm test:watch`| Roda os testes em modo observação.                |
| `pnpm lint`      | Roda o Oxlint.                                    |

## Telas

| Rota                         | Tela                  | O que faz |
| ---------------------------- | --------------------- | --------- |
| `/login`                     | Login                 | Autenticação; o token fica guardado na sessão do navegador. |
| `/register`                  | Cadastro              | Criação de usuário. |
| `/trocar-senha`              | Trocar senha          | Pública (é também o caminho de quem não consegue entrar), mas exige email + **senha atual**. |
| `/dashboard`                 | Visão geral           | Peças em estoque, entregas do mês, alunos, matriz de tamanhos, últimas entregas e "Repor primeiro" com o botão **Solicitar** (abre `/requisicoes/nova` preenchida). |
| `/pedidos`                   | Entregas              | Entregas registradas; o detalhe abre num painel (`?ver=id`). Aluno que saiu aparece com o selo "Saiu". |
| `/pedidos/nova`              | Nova entrega          | Aluno (só ativos) + peças pelo catálogo; tamanhos sem estoque bloqueados e quantidade limitada ao saldo. |
| `/entradas`                  | Entradas              | Lotes recebidos do fornecedor, com nota fiscal e itens. É o que faz o estoque subir. |
| `/entradas/nova`, `/entradas/:id/editar` | Nova entrada / editar | Lançamento em linhas ou em grade tamanho × sexo; peças repetidas são somadas (com aviso). |
| `/estoque`                   | Estoque               | Cada combinação de tipo, tamanho e sexo, com a situação e o atalho "Repor". |
| `/requisicoes`               | Requisições           | Abas "Em andamento" (pendentes) e "Atendidas e canceladas"; marcar como atendida ou cancelar. |
| `/requisicoes/nova`          | Nova requisição       | Tipo, tamanho, sexo, quantidade e observação. Aceita `?tipoUniformeId=&sexo=&tamanho=` para vir preenchida. |
| `/alunos`                    | Alunos                | Lista com busca por nome ou turma (no servidor, sem diferenciar acentos) e filtros de turma e situação; detalhe com histórico de entregas; **Registrar saída** / **Reativar**. |
| `/alunos/novo`, `/alunos/:id/editar` | Novo aluno / editar | Cadastro com turma, responsável e telefone. |
| `/turmas`, `/turmas/nova`, `/turmas/:id/editar` | Turmas | CRUD de turmas. |
| `/tipos-uniforme`, `/tipos-uniforme/novo`, `/tipos-uniforme/:id/editar` | Tipos de uniforme | CRUD dos tipos (camiseta, calça, agasalho…) e do limite de estoque baixo de cada um. |
| `/relatorios`                | Relatórios            | Estoque, entradas, saídas, entregas por turma e transações, em **PDF ou CSV**. |
| `/conta`                     | Conta                 | Dados do usuário logado (pode manter o próprio email). |

Todas as rotas exceto `/login`, `/register` e `/trocar-senha` passam por `ProtectedRoute` e redirecionam para o login sem sessão válida.

### Sessão expirada

Quando a API responde `401` (token ausente, inválido ou vencido), `lib/api-client.ts` chama `expireSession()`: a sessão é limpa, o usuário volta para `/login` e vê o aviso de sessão expirada. A sessão também expira sozinha no horário do `exp` do token.

### Alertas de estoque

O limite de "estoque baixo" é **por tipo de uniforme** (campo `estoqueBaixo`, editado em Tipos de uniforme) — não existe limite fixo. A classificação em `lib/estoque.ts` (`nivelEstoque(quantidade, limite)`) segue a mesma regra da VIEW `vw_estoque` do banco:

| Nível | Regra |
| ----- | ----- |
| **Esgotado** | `quantidade <= 0` |
| **Baixo** | `quantidade <= limite` (o próprio limite já conta como baixo) |
| **OK** | `quantidade > limite` |

A classificação alimenta os selos da tela de estoque, o card "Repor primeiro" (com o botão "Solicitar") e o contador de tamanhos esgotados na visão geral.

## Estrutura de pastas

```
src/
├── components/
│   ├── ui/           # Componentes do shadcn/ui (button, dialog, table…)
│   ├── alunos/       # Componentes de cada domínio: formulários e filtros
│   ├── dashboard/
│   ├── entradas/
│   ├── pedidos/
│   ├── relatorios/
│   ├── shared/       # Confirmação de exclusão, mensagens de limite etc.
│   ├── turmas/
│   ├── tipos-uniforme/
│   ├── auth/         # Login, cadastro e proteção de rota
│   └── layout/       # Casca da aplicação (sidebar, cabeçalho)
├── context/          # AuthProvider
├── hooks/            # Um hook de query/mutation por domínio (use-alunos, use-lotes…)
├── lib/
│   ├── schemas/      # Schemas Zod dos formulários
│   ├── types/        # Tipos das respostas da API
│   ├── api-client.ts # fetch com token, tratamento de erro e 401
│   └── estoque.ts    # Regras de nível de estoque e agrupamentos
├── pages/            # Uma tela por rota
├── services/         # Chamadas à API, uma função por endpoint
└── test/             # Helpers de teste (render com providers)
```

## Testes

```bash
pnpm test
```

A suíte usa Vitest com jsdom e Testing Library. Os serviços são mockados com `vi.mock`, de modo que nenhum teste depende do backend no ar.

São **86 testes** em 19 arquivos:

| Arquivo | O que cobre |
| ------- | ----------- |
| `lib/api-client.test.ts` | Token no cabeçalho, erros da API e expiração da sessão no `401`. |
| `lib/auth.test.ts` | Leitura do `exp` do JWT e sessão vencida. |
| `lib/estoque.test.ts` | Esgotado / baixo / ok por limite do tipo (inclui `quantidade = limite`), agrupamentos e "Repor primeiro". |
| `lib/entrega.test.ts` | Soma da mesma peça em várias linhas, teto pelo estoque, aluno e peça obrigatórios. |
| `lib/alunos.test.ts` | Normalização da busca: sem acentos nem caixa, por nome do aluno ou da turma. |
| `lib/types/page.test.ts` | Paginação. |
| `components/auth/login-form.test.tsx` | Formulário de login e mensagens de erro. |
| `components/auth/protected-route.test.tsx` | Redirecionamento sem sessão ou com sessão expirada. |
| `pages/alunos-page.test.tsx` | Busca no servidor sem acentos, filtros de turma e situação, desligados com data de saída, registrar saída e reativar. |
| `pages/aluno-form-page.test.tsx` | Cadastro e edição de aluno. |
| `pages/pedidos-page.test.tsx` | A lista de entregas continua de pé quando uma linha vem com dados incompletos (aluno removido). |
| `pages/nova-entrega-page.test.tsx` | Bloqueio de tamanho sem estoque, teto de quantidade e aluno desligado. |
| `pages/entrada-form-page.test.tsx` | Entrada em linhas e pela grade, peça repetida em duas linhas, campos da nota obrigatórios. |
| `pages/requisicoes-page.test.tsx` | Estado vazio, lista em andamento com solicitante e status, troca para "Atendidas e canceladas". |
| `pages/requisicao-form-page.test.tsx` | Nova requisição, validação e preenchimento pelo "Solicitar". |
| `pages/relatorios-page.test.tsx` | Download dos relatórios em PDF e CSV, período, turma e mensagens de erro. |
| `pages/tipo-uniforme-form-page.test.tsx` | Limite de estoque baixo: envio, prévia dos alertas e recusa de negativo. |
| `pages/trocar-senha-page.test.tsx` | Troca de senha com senha atual. |
| `pages/conta-page.test.tsx` | Salvar mantendo o próprio email, encerrar a sessão ao trocar o email, troca de senha com senha atual. |

## Padrões usados no código

- **Serviço → hook → tela.** `services/` conhece a API, `hooks/` embrulha em TanStack Query e cuida da invalidação de cache, e a tela só consome. Nenhuma página chama `fetch` diretamente.
- **Validação no Zod, mensagens em português.** Cada formulário tem um schema em `lib/schemas/` e usa `zodResolver`; os erros aparecem junto ao campo, com `aria-invalid` no input.
- **Erro do backend aparece para o usuário.** `apiFetch` levanta `ApiError` com a mensagem que a API devolveu, e as telas mostram essa mensagem no toast em vez de um texto genérico.
- **Toda listagem trata os quatro estados**: carregando (skeleton), erro, vazio e preenchido. O texto do estado vazio muda quando há filtro aplicado.
- **Alias `@/`** aponta para `src/`.

## Outros READMEs do projeto

- [README do projeto](../README.md)
- [Documentação do backend](../backend/BACKEND.md)
- [Documentação dos endpoints](../docs/endpoints.md)
