# 6M Uniformes — Frontend

Interface web do sistema de gestão de uniformes escolares. Cobre o ciclo completo do almoxarifado: a entrada dos lotes do fornecedor, a entrega dos uniformes aos alunos, o acompanhamento do estoque por tamanho e a emissão dos relatórios em PDF.

Consome a API documentada em [BACKEND.md](../backend/BACKEND.md).

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
- **pnpm** (o repositório versiona um `pnpm-lock.yaml`)
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

Na primeira execução não há usuário cadastrado: acesse `/register` para criar o seu e depois entre por `/login`.

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

| Rota             | Tela               | O que faz                                                                            |
| ---------------- | ------------------ | ------------------------------------------------------------------------------------ |
| `/login`         | Login              | Autenticação; o token fica guardado na sessão do navegador.                           |
| `/register`      | Cadastro           | Criação de usuário.                                                                   |
| `/trocar-senha`  | Trocar senha       | Pública, porque é também o caminho de quem não consegue entrar.                       |
| `/dashboard`     | Visão geral        | Peças em estoque, entregas do mês, alunos, matriz de tamanhos e lista de reposição.    |
| `/entradas`      | Entradas           | Lotes recebidos do fornecedor, com nota fiscal e itens. É o que faz o estoque subir.   |
| `/pedidos`       | Entregas           | Entregas registradas e o formulário de nova entrega (aluno + peças).                   |
| `/estoque`       | Estoque            | Cada combinação de tipo, tamanho e sexo, com a situação de cada uma.                   |
| `/alunos`        | Alunos             | CRUD de alunos, com busca por nome ou turma.                                           |
| `/turmas`        | Turmas             | CRUD de turmas.                                                                        |
| `/tipos-uniforme`| Tipos de uniforme  | CRUD dos tipos (camiseta, calça, agasalho…).                                            |
| `/relatorios`    | Relatórios         | Geração dos PDFs: estoque, entradas, saídas, entregas por turma e transações.           |
| `/conta`         | Conta              | Dados do usuário logado.                                                               |

Todas as rotas exceto `/login`, `/register` e `/trocar-senha` passam por `ProtectedRoute` e redirecionam para o login sem sessão válida.

### Alertas de estoque

O estoque é classificado em `lib/estoque.ts`: **esgotado** (0 peças) e **baixo** (até 5 peças, `LIMITE_ESTOQUE_BAIXO`). A classificação alimenta os selos da tela de estoque, o card "Repor primeiro" e o contador de tamanhos esgotados na visão geral.

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

O que está coberto: o formulário de entrada de lote, o de entrega (incluindo o bloqueio de tamanho sem estoque e o teto de quantidade), a busca de alunos por nome e turma, a geração de relatórios, o `api-client` (token, erros e limpeza de sessão no 401) e a paginação.

## Padrões usados no código

- **Serviço → hook → tela.** `services/` conhece a API, `hooks/` embrulha em TanStack Query e cuida da invalidação de cache, e a tela só consome. Nenhuma página chama `fetch` diretamente.
- **Validação no Zod, mensagens em português.** Cada formulário tem um schema em `lib/schemas/` e usa `zodResolver`; os erros aparecem junto ao campo, com `aria-invalid` no input.
- **Erro do backend aparece para o usuário.** `apiFetch` levanta `ApiError` com a mensagem que a API devolveu, e as telas mostram essa mensagem no toast em vez de um texto genérico.
- **Toda listagem trata os quatro estados**: carregando (skeleton), erro, vazio e preenchido. O texto do estado vazio muda quando há filtro aplicado.
- **Alias `@/`** aponta para `src/`.

## Outros READMEs do projeto

- [README do projeto](../README.md)
- [Documentação do backend](../backend/BACKEND.md)
