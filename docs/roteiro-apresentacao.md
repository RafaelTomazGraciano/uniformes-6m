# Roteiro da apresentação final — Uniformes 6M

**Duração total: 25 minutos**

| Bloco | Tempo | Quem | Conteúdo |
| ----- | ----- | ---- | -------- |
| 1 | 10 min | Product Owner | Demonstração ao vivo do sistema |
| 2 | 5 min  | Dev back-end  | Decisões técnicas: banco, API e maior desafio |
| 3 | 5 min  | Scrum Master  | Processo: sprints, métricas, aprendizados |
| 4 | 5 min  | Todos         | Perguntas da banca |

> Slides de apoio: deck de slides com as screenshots de cada funcionalidade (publicado no Claude).

> Regra de ouro: quem não está falando cuida do relógio. Combinem um sinal discreto para "faltam 2 minutos" e outro para "acabou o tempo".

---

## 0. Checklist antes da apresentação

Fazer **no dia, pelo menos 30 minutos antes**, na máquina que vai projetar.

### Subir o sistema do zero

```bash
# 1. Backend + banco (Docker precisa estar aberto)
cd backend
cp .env.example .env            # só se ainda não existir
docker compose down -v          # ATENÇÃO: apaga o banco; garante uma base limpa e previsível
docker compose up -d --build     # o Flyway aplica as migrations V1 a V5
docker compose logs -f backend  # espere "Started UniformApplication" e saia com Ctrl+C

# 2. Dados de demonstração (usuário, 5 tipos, 7 turmas, 46 alunos, 2 lotes, 32 entregas)
cd ..
node backend/scripts/seed.mjs
# Saída esperada termina com: "Pronto. Entre com admin@uniformes6m.local / senha123"

# 3. Frontend
cd frontend
pnpm install                    # sem pnpm instalado: npx -y pnpm@10 install
cp .env.example .env            # só se ainda não existir
pnpm dev                        # http://localhost:5173
```

O seed é determinístico: num banco limpo, gera sempre os mesmos alunos e quantidades. Se o banco já tiver tipos de uniforme, o script não faz nada (use `--force` só se souber o que está fazendo).

### Conferir

- [ ] `http://localhost:8080/swagger-ui.html` abre (prova de que a API está no ar).
- [ ] Login em `http://localhost:5173` com **admin@uniformes6m.local / senha123** funciona e a Visão geral mostra números (não zeros).
- [ ] Fazer login de novo logo antes de começar (o token dura 10 h). Se vencer no meio da demo, a API responde `401` e o sistema volta sozinho ao login com o aviso "sessão expirada" — basta entrar de novo.
- [ ] Ensaiar o roteiro uma vez e **anotar** nos espaços abaixo os dados reais escolhidos (aluno, tamanho, quantidades), para não improvisar ao vivo.
- [ ] Ter uma chave de NF-e de 44 dígitos copiada (ex.: `35261112345678000190550010000099991000099999`) — chave repetida é recusada.
- [ ] Navegador em tela cheia, zoom 110–125%, sem abas e notificações; modo claro (projetores lavam o modo escuro).
- [ ] Pasta de downloads limpa (o PDF/CSV baixado vai aparecer ali).
- [ ] Plano B: gravação em vídeo da demo ensaiada e prints das telas, caso Docker ou rede falhem.

### Dados do ensaio (preencher)

| O quê | Valor anotado no ensaio |
| ----- | ----------------------- |
| Tipo/sexo/tamanho que está "Baixo" para repor na entrada | __________________ (saldo antes: ___ / limite: ___) |
| Quantidade lançada na nova entrada | ___ peças |
| Aluno para a nova entrega (nome e turma) | __________________ |
| Peça entregue que vai cair abaixo do limite | __________________ (saldo antes: ___ → depois: ___) |
| Turma usada na busca e no relatório | __________________ |
| Peça do "Repor primeiro" para a requisição | __________________ (quantidade: ___) |
| Aluno para registrar a saída (de preferência um que já recebeu uniforme) | __________________ |

---

## 1. Demonstração ao vivo — Product Owner (10 min)

**Fio condutor para narrar:** "Vamos acompanhar um dia da Rosa, a almoxarife da escola: chega um lote do fornecedor, ela entrega uniforme a um aluno, o sistema avisa que um tamanho está acabando, ela já pede a reposição ao fornecedor, um aluno sai da escola e, no fim do mês, a coordenação pede o relatório de entregas por turma."

### Passo 1 — Login (0:00–0:45)

- Abrir `http://localhost:5173/login`.
- Entrar com `admin@uniformes6m.local` / `senha123`.
- **Falar:** "Todas as telas são protegidas; sem login, ou com a sessão expirada, o sistema volta para cá. A API usa token JWT."

### Passo 2 — Visão geral / dashboard (0:45–2:00)

- Mostrar os quatro números do topo: **peças em estoque**, **entregas no mês**, **alunos cadastrados**, **tamanhos esgotados**.
- Mostrar a matriz **Estoque por tamanho** e o card **Repor primeiro**.
- Mostrar **Últimas entregas**.
- **Falar:** "É a tela do coordenador: em cinco segundos ele sabe o que tem, o que saiu e o que precisa comprar."
- **Anotar mentalmente** o total de peças em estoque — vamos voltar aqui.

### Passo 3 — Nova entrada de lote (2:00–4:00)

- Clicar em **Nova entrada** (botão no topo da Visão geral).
  - Alternativa mais "uau": em **Estoque**, filtrar **Repor (baixo ou esgotado)** e clicar em **Repor** na linha anotada — a nova entrada abre já com o tipo, o sexo e o tamanho preenchidos.
- Preencher **Fornecedor** (ex.: "Confecções Aurora"), **Data de entrega** (hoje) e **Chave de acesso da NF-e** (colar a chave preparada).
- Mostrar as duas formas de lançar: **Linhas** e **Grade** (tamanho × sexo, como vem na nota). Lançar na grade a quantidade anotada.
- Clicar em **Registrar entrada** e mostrar o toast de sucesso.
- **Falar:** "A nota fiscal fica vinculada ao lote; a mesma chave não pode ser lançada duas vezes."

### Passo 4 — Estoque sobe (4:00–5:00)

- Ir em **Estoque**. Mostrar a linha reposta: saldo maior e o selo **Baixo** sumiu.
- Alternar a visualização **Tabela/Grade**.
- Voltar rapidamente à **Visão geral**: o total de peças em estoque aumentou exatamente o que foi lançado.
- **Falar:** "Ninguém digitou o saldo. O sistema somou na mesma transação em que gravou o lote."

### Passo 5 — Nova entrega para um aluno (5:00–7:00)

- Clicar em **Nova entrega** (ou, em **Alunos**, menu do aluno → **Nova entrega**, que já abre com o aluno escolhido).
- Selecionar o aluno anotado.
- Na aba **Catálogo**, mostrar que **tamanhos sem estoque ficam bloqueados** e que a **quantidade não passa do saldo**.
- Adicionar a peça anotada (a que vai cair abaixo do limite) e talvez mais uma.
- O rodapé mostra "N peças para <aluno>". Clicar em **Registrar entrega**.
- **Falar:** "A entrega registra o aluno, as peças, a data e quem lançou. Se alguém tentar entregar mais do que existe — mesmo pela API, sem a tela — o backend recusa."

### Passo 6 — Estoque baixo, alerta e requisição (7:00–8:00)

- Voltar à **Visão geral**: a peça entregue agora aparece em **Repor primeiro**; "entregas no mês" aumentou.
- **Falar:** "Cada tipo tem seu próprio limite — avisar com 8 bermudas, mas com 3 jaquetas. Chegar no limite já conta como baixo."
- No card **Repor primeiro**, clicar em **Solicitar** na peça anotada: abre **Nova requisição** já com tipo, sexo e tamanho. Informar a quantidade e uma observação e salvar.
- Em **Requisições**, mostrar a aba **Em andamento** com a requisição pendente (quem pediu e quando) e a aba **Atendidas e canceladas**; marcar uma como **Atendida** (ou **Cancelar**).
- **Falar:** "A requisição registra o que pedimos ao fornecedor. Ela não mexe no estoque: as peças só entram quando o lote chega, como vimos no passo 3."

### Passo 7 — Busca de aluno e saída da escola (8:00–9:00)

- Ir em **Alunos**. Digitar parte de um nome no campo **Buscar por nome ou turma** (ex.: "joao" encontra "João" — a busca ignora acentos).
- Abrir o aluno anotado e clicar em **Registrar saída**. Mostrar o selo **Saiu em dd/mm/aaaa** e que o histórico de entregas continua ali.
- Em **Nova entrega**, mostrar que ele não aparece mais na lista de alunos (e a API recusa entrega para aluno desligado, pedindo para reativar o cadastro).
- **Falar:** "Antes, 'excluir' um aluno que já tinha recebido uniforme quebrava o histórico. Agora quem sai é desligado, não apagado: os relatórios continuam fechando. Excluir ficou só para cadastro feito por engano."

### Passo 8 — Relatório de entregas por turma em PDF e CSV (9:00–10:00)

- Ir em **Relatórios**. Mostrar os cinco: **Estoque**, **Entradas**, **Saídas**, **Entregas por turma**, **Transações**.
- Em **Entregas por turma**, escolher o mês atual e **Todas as turmas** e gerar o **PDF**: entregas agrupadas por turma com subtotal — a entrega de agora está lá.
- Gerar o mesmo em **CSV** e mostrar que abre direto no Excel.
- **Fechamento do PO:** "Do lote que chegou ao arquivo da prestação de contas, sem planilha e sem conta à mão. Passo para o Rafael, que vai explicar como isso funciona por dentro."

> Se algo falhar ao vivo: não depurar na frente da banca. Dizer "vou mostrar pelo vídeo do ensaio" e seguir. A mensagem de erro em português na tela também é uma funcionalidade — pode virar parte da demo.

---

## 2. Decisões técnicas — Dev back-end (5 min)

### Arquitetura em um slide (1 min)

- **Backend:** Java 25 + Spring Boot, API REST, rodando em Docker com PostgreSQL.
- **Frontend:** React 19 + TypeScript + Vite, TanStack Query para cache, React Hook Form + Zod para formulários.
- Organização por **domínio** (`aluno`, `turma`, `lote`, `pedido`, `uniforme`, `requisicao`, `relatorio`…), cada um com `Controller → Service → Repository` e DTOs (`records`) de entrada e saída. A entidade nunca vaza para a API.

### Banco de dados (1 min)

- **PostgreSQL**: relacional porque o domínio é relacional (lote → itens → tipo; pedido → itens → uniforme → tipo; aluno → turma) e precisamos de transações e restrições fortes.
- **Flyway** versiona o schema em migrations V1 a V5 (requisição, saída de aluno, VIEW de estoque, datas obrigatórias); o Hibernate só valida (`ddl-auto=validate`), não altera o banco sozinho.
- **Enums nativos do Postgres** (tamanho, sexo, turno…), **UUID** como chave e **constraints de unicidade** (nome de turma, tipo de uniforme, chave da NF-e, combinação tipo + tamanho + sexo).
- **Saída de aluno em vez de exclusão** (`aluno.ativo`, `data_saida`): o bug que motivou isso — o soft delete com `@SQLRestriction` escondia o aluno também dentro das entregas — e a migration V3 que recuperou os alunos afetados.
- **VIEW `vw_estoque`**: a regra ESGOTADO / BAIXO (`quantidade <= limite`) / OK fica num lugar só, usada pelo painel e pelo relatório.
- Mostrar o DER (`docs/DER.png`, 12 tabelas + a view).

### API (1 min)

- REST com recursos no singular (`/api/aluno`, `/api/lote`…), **paginação e ordenação** em todas as listagens (`?page=0&size=20&sort=nome,asc`).
- **JWT stateless** (Spring Security), token de 10 h; o usuário de cada entrega e requisição vem do token, nunca do corpo. Token ausente/expirado → `401` com JSON, que o front usa para voltar ao login.
- **Bean Validation** nos DTOs + `RestExceptionHandler` global: todo erro sai em JSON padronizado com mensagem em português, que o front mostra direto no toast.
- Relatórios gerados no servidor em **PDF** (`PdfTableBuilder`) ou **CSV** (`CsvTableBuilder`), sem erro quando o período está vazio.
- Documentação: Swagger (springdoc) + `docs/endpoints.md`.

### Maior desafio técnico: manter o estoque correto sob concorrência (2 min)

**O problema.** O estoque é um número que várias operações mexem ao mesmo tempo. Duas pessoas no balcão entregando a última camiseta M ao mesmo tempo: as duas leem "1", as duas gravam "0" — e uma peça que não existe foi entregue (*lost update*). E na entrada: dois lotes com uma combinação nova (ex.: jaqueta feminina PP) chegando juntos podiam criar duas linhas de estoque para a mesma peça.

**Como resolvemos** (PRs #13 e #19):

1. **Transação única por operação.** `criarPedido` e `criarLote` são `@Transactional`: grava o cabeçalho, os itens e mexe no estoque; se qualquer item falhar (ex.: saldo insuficiente), tudo é desfeito.
2. **Lock pessimista na saída.** O `UniformeRepository` busca a linha com `@Lock(PESSIMISTIC_WRITE)` (um `SELECT ... FOR UPDATE`). A segunda entrega espera a primeira terminar, lê o saldo já atualizado e é recusada com "Quantidade solicitada (1) maior que o estoque disponível (0)".
3. **Constraint no banco como última barreira.** `uq_uniforme_tipo_tamanho_sexo` impede duas linhas para a mesma combinação; se a corrida acontecer, o handler traduz a violação em `409 — "Este uniforme está sendo registrado por outra operação neste momento. Tente novamente"` em vez de um erro 500.
4. **Locks sempre na mesma ordem.** Antes de mexer no saldo, todas as linhas da entrega/lote são travadas ordenadas por id: duas entregas com as mesmas peças em ordens diferentes não entram em deadlock.
5. **Correções que não quebram o saldo.** Editar um lote ou uma entrega substitui os itens com estorno: no lote, se as peças antigas já foram entregues e o estorno deixaria o estoque negativo, a correção é recusada.
6. **Teste de integração com banco real.** `FluxoCompletoIntegrationTest` (com `SegurancaIntegrationTest` e `AlunoSaidaIntegrationTest`) usa **Testcontainers** com Postgres de verdade (H2 não suporta os enums nativos nem o lock da mesma forma): registra, loga, dá entrada, entrega e confere o saldo e os PDFs.

**Frase de fechamento:** "A regra 'o estoque nunca fica negativo' não depende da tela: está garantida no banco e no serviço, mesmo que duas pessoas cliquem ao mesmo tempo."

---

## 3. Processo — Scrum Master (5 min)

> Preencher os campos `[ ]` com os dados do quadro (Linear/Trello/Jira) antes da apresentação. Os números marcados como "do git" já foram conferidos no repositório.

### Como trabalhamos (1 min)

- Papéis: Pedro (Product Owner), Juliana (Scrum Master), Rafael (Back-end e Banco de Dados), Gabriel Witor (Front-end e Integração), Giovana (Design de Interface e UX), Ana Júlia (QA).
- Duração da sprint: [ ] semanas · Total de sprints: [ ]
- Cerimônias: planning [quando], daily [formato/frequência], review [quando], retrospectiva [quando].
- Ferramentas: [quadro], GitHub (branch `develop` + PRs para integração, `main` para entrega), WhatsApp/Discord [ ].
- Definição de pronto (DoD): código revisado em PR, testes passando, endpoint documentado no Swagger, tela tratando carregando/erro/vazio.

### Métricas (2 min)

| Métrica | Valor |
| ------- | ----- |
| Período do projeto (do git) | 09/06/2026 a 19/10/2026 |
| Commits na `develop` (do git) | 34 |
| Pull requests integrados (do git) | 21 |
| Testes automatizados | 235 no backend + 86 no frontend |
| Histórias planejadas × entregues | [ ] × [ ] |
| Pontos planejados × entregues (total) | [ ] × [ ] |
| Velocidade média por sprint | [ ] pontos |
| Burndown da última sprint | [inserir gráfico] |
| Bugs encontrados / corrigidos | [ ] / [ ] |
| Itens que ficaram no backlog | [ ] (ex.: perfis de acesso, multi-escola) |

Sugestão de slide: gráfico de velocidade por sprint + burndown da sprint final.

### Evolução por sprint (1 min) — linha do tempo pelo git

| Sprint | Entregas principais (do histórico de PRs) |
| ------ | ----------------------------------------- |
| [ 1 ] | Entidades, repositories, login/registro com JWT, backend em Docker |
| [ 2 ] | CRUD de turma, aluno, uniforme e lote com testes unitários; setup do frontend (login, cadastro, sidebar) |
| [ 3 ] | Swagger, itens de lote, pedidos com baixa de estoque, troca de senha |
| [ 4 ] | Relatórios em PDF; telas de alunos, entregas, nova entrega e estoque |
| [ 5 ] | Relatório de entregas por turma, contato do responsável, estoque baixo por tipo, refatoração de acessibilidade (WCAG/shadcn), acesso em rede local |
| [ 6 ] | Requisição de novos uniformes, saída de aluno, VIEW de estoque, relatórios em CSV, 401 para sessão expirada, troca de senha com senha atual, migrations V2–V5 |

### Aprendizados e retrospectiva (1 min)

- O que funcionou: [ex.: PRs pequenos por domínio facilitaram a revisão]
- O que foi difícil: [ex.: integrar front e back quando o contrato da API mudava → passamos a documentar no Swagger antes]
- O que faríamos diferente: [ ]
- Impedimentos relevantes e como foram removidos: [ ]

---

## 4. Perguntas da banca (5 min) — respostas sugeridas

**Quem responde:** produto → PO; banco/API/segurança → back-end; telas/acessibilidade → front-end; prazos/equipe → Scrum Master.

| Pergunta provável | Resposta sugerida |
| ----------------- | ----------------- |
| **E se duas pessoas entregarem a última peça ao mesmo tempo?** | Lock pessimista (`SELECT ... FOR UPDATE`) na linha do estoque dentro da transação: a segunda espera, vê saldo zero e é recusada. Ver bloco 2. |
| **E se der deadlock entre duas entregas com várias peças?** | Não dá: todas as linhas de estoque envolvidas são travadas antes, sempre na mesma ordem (ordenadas por id), então não há espera circular. |
| **Por que PostgreSQL e não MongoDB/NoSQL?** | O domínio é relacional e o requisito central é consistência transacional do saldo; constraints e locks do Postgres fazem isso nativamente. |
| **Por que UUID e não ID numérico?** | Não expõe a quantidade de registros, evita IDs adivinháveis em URL e permite gerar IDs fora do banco se integrarmos outros sistemas. |
| **Por que não dá para excluir uma entrega ou um lote?** | São o registro da prestação de contas. Erros são corrigidos com edição, que estorna e relança o estoque. Pelo mesmo motivo, aluno que sai da escola é desligado ("Registrar saída"), não excluído; excluir só vale para cadastro feito por engano e é recusado se ele já recebeu uniforme. |
| **Como funciona o alerta de estoque baixo?** | Cada tipo de uniforme tem um limite. A VIEW `vw_estoque` calcula a situação: 0 = ESGOTADO, `quantidade <= limite` = BAIXO, acima = OK. O painel e o relatório leem a view; o card "Repor primeiro" tem o botão "Solicitar". |
| **A requisição dá entrada no estoque?** | Não, de propósito: o estoque só sobe com o lote (nota fiscal). A requisição é o registro do que foi pedido e muda de pendente para atendida ou cancelada. |
| **Como a senha é guardada? Como funciona o login?** | Senha com hash (BCrypt via Spring Security); login devolve JWT assinado que expira em 10 h e vai no header `Authorization: Bearer`. |
| **A troca de senha não exige login. Isso não é inseguro?** | Ela exige a senha atual; email inexistente e senha errada dão a mesma resposta, para não revelar quais emails existem. Recuperação de quem **esqueceu** a senha (link por email) está no backlog. |
| **O que acontece quando o token expira?** | A API responde `401` com JSON e o front volta ao login com o aviso de sessão expirada. `403` fica para falta de permissão. |
| **Existe controle de perfis (admin × consulta)?** | Não nesta versão: todo usuário autenticado tem o mesmo acesso. Está no "fora de escopo" do documento de visão e é o próximo item do backlog. |
| **Funciona para várias escolas?** | A entidade Escola já existe no modelo, mas o sistema opera como uma unidade. Multi-escola exigiria vincular turmas/estoque à escola e filtrar por ela. |
| **E se a escola tiver 5.000 alunos?** | A API é paginada em todas as listagens. A busca de alunos já é feita no servidor (`GET /api/aluno?nome=&turmaId=&ativo=`, sem diferenciar acentos), então a tela não carrega todos os alunos. |
| **Como vocês testaram?** | Backend (235 testes): unitários dos services com Mockito e integração com Testcontainers (Postgres real) — fluxo completo, segurança (401, trocar-senha) e saída de aluno. Frontend (86 testes): Vitest + Testing Library cobrindo entrada e entrega, requisições, alunos, relatórios PDF/CSV, sessão expirada e o cliente HTTP. A QA documentou os testes manuais em `docs/qa/`. |
| **Onde está a documentação da API?** | Swagger em `/swagger-ui.html` e `docs/endpoints.md` com todos os endpoints, parâmetros, exemplos e erros. |
| **Como o sistema é implantado?** | `docker compose up` sobe Postgres e API; o front é um build estático do Vite. Na escola, roda num computador da rede local e os outros acessam pelo IP. |
| **O que fariam com mais uma sprint?** | Perfis de acesso, recuperação de senha por email, ligar a requisição ao lote que a atendeu, leitura do XML da NF-e para preencher a entrada automaticamente. |
| **Qual foi a maior dificuldade do time (não técnica)?** | Scrum Master responde com o item da retrospectiva. [preencher] |
| **Como vocês dividiram o trabalho?** | Scrum Master: por domínio/história, com PR por funcionalidade e revisão cruzada. [ajustar com a realidade do time] |

---

## Material de apoio

- [Documento de visão do produto](./visao-do-produto.md)
- [Documentação dos endpoints](./endpoints.md)
- [ERD](./ERD.md), [DER.png](./DER.png) e [decisões de modelagem](./decisoes-de-modelagem.md)
- [Relatório de qualidade](./relatorio-qualidade.md)
- [README do frontend](../frontend/README.md) — lista das telas
- [BACKEND.md](../backend/BACKEND.md) — DER, autenticação, testes
