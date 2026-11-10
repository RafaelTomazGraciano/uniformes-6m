# Relatório de qualidade — Uniformes 6M

Entrega de QA (S7). Reteste do roteiro funcional CT01–CT17 depois da rodada de correções, com os testes automatizados e a situação dos bugs.

- **Data do reteste:** 10/11/2026
- **Ambiente:** frontend `http://localhost:5173` (Vite), API `http://localhost:8080/api` (Spring Boot em Docker, PostgreSQL 18), branch `develop` com as correções aplicadas.
- **Usuário:** `admin@uniformes6m.local`
- **Ferramenta:** Playwright (Chromium) dirigindo a interface real; chamadas diretas à API só nos casos que pedem verificação no nível da API (CT02, CT04, CT06, CT12) ou para conferir o resultado de uma ação feita na tela.
- **Massa de dados própria do reteste:** tipo de uniforme **Colete QA** (limite de estoque baixo = 5), alunos **Rafael Teste QA** e **Bianca Teste QA** (6º ano A). Ela não depende das contagens da demonstração, que outra pessoa estava alterando ao mesmo tempo.
- **Evidências:** `docs/qa/evidencias/CTxx*.png`

---

## 1. Resumo

| Situação | 1ª rodada (Ana Júlia) | Reteste |
| --- | :---: | :---: |
| Aprovado | 4 | **14** |
| Reprovado | 3 | **0** |
| Parcial | 6 | **0** |
| Não confirmado | 3 | **0** |
| Não executado | 1 | **0** |
| Não aplicável (regra pendente do PO) | — | **3** |
| **Total** | **17** | **17** |

- Todos os casos executáveis passaram: os 14 que têm regra definida foram aprovados pela interface real, sem erro de JavaScript no console e sem resposta 5xx da API durante as execuções.
- CT13, CT14 e CT15 (limite de uniformes por aluno) ficam **Não aplicável**: essa regra não está nos requisitos (cronograma do curso e relatório). Depende de definição do PO.
- Um dos aprovados tem ressalva: no CT04, a mensagem da tela é "Quantidade mínima é 1." e não "Quantidade é obrigatória" (bug B13, baixa — corrigido: a tela agora mostra "Quantidade é obrigatória (mínimo 1).").
- O reteste encontrou **3 bugs novos** (B12 média, B13 e B14 baixas) e **reabriu o B11**. Os quatro foram corrigidos logo depois do reteste (ver a seção 4).

## 2. Testes automatizados

### Backend: 235 testes, 0 falhas, 21 classes

Suíte JUnit 5 do `backend/` (`./gradlew test`). Ela não foi executada de novo neste reteste: o resultado conhecido da rodada de correções é **235 testes, 0 falhas, 0 erros, em 21 classes**.

| Área | Classes | O que cobre |
| --- | --- | --- |
| Unitários de serviço (JUnit + Mockito) | `AlunoServiceTest`, `EscolaServiceTest`, `ItemLoteServiceTest`, `LoteServiceTest`, `NotaFiscalServiceTest`, `PedidoServiceTest`, `PedidoUniformeServiceTest`, `RelatorioServiceTest`, `RelatorioPeriodoResolverTest`, `RequisicaoServiceTest`, `TipoUniformeServiceTest`, `TurmaServiceTest`, `UniformeServiceTest`, `UsuarioServiceTest` | Regras de negócio de cada serviço com repositórios simulados. Inclui os relatórios vazios (`deveGerarRelatorioEstoqueMesmoComEstoqueVazio`, `…EntradaVazio…`, `…SaidaVazioEmCsv…`, `…EntregasVazio…`, `…TransacoesVazio…`), a requisição e a saída de aluno. |
| Segurança e utilitários | `JwtTokenServiceTest`, `UserDetailsServiceImplTest`, `CsvTableBuilderTest` | Emissão e validação do JWT, carga do usuário, geração de CSV (BOM, `;`, "Nenhum registro encontrado"). |
| Integração (Spring Boot + Testcontainers/PostgreSQL) | `FluxoCompletoIntegrationTest`, `SegurancaIntegrationTest`, `AlunoSaidaIntegrationTest` (base: `AbstractIntegrationTest`) | Fluxo real com banco: cadastro → entrada → entrega → estoque → relatórios em PDF e CSV, incluindo período sem movimento. Também 401 para token ausente, inválido ou expirado, troca de senha exigindo a senha atual, saída de aluno mantendo o histórico e o bloqueio de exclusão. |

### Frontend: 86 testes, 19 arquivos, 0 falhas

`cd frontend && npx -y pnpm@10 test` (Vitest 5 + Testing Library), executado em 10/11/2026:

```
Test Files  19 passed (19)
     Tests  86 passed (86)
```

Cobre `lib/` (api-client com o tratamento do 401, auth, alunos, entrega, estoque) e as páginas de formulário e lista (alunos, aluno, conta, entrada, nova entrega, entregas, relatórios, requisição, requisições, tipo de uniforme, troca de senha), além de `login-form` e `protected-route`.

### Cobertura (JaCoCo, backend)

| Métrica | Valor |
| --- | --- |
| Linhas | 52,9% (1669/3156) |
| Instruções | 47,9% |
| Funções (métodos) | 46,5% |
| Ramos | 43,0% |

> Esses números vêm do relatório original e foram medidos **antes** desta rodada de correções. Desde então entraram testes novos (requisição, saída de aluno, CSV, segurança), então a cobertura atual tende a ser maior. Para atualizar, rode de novo `./gradlew test jacocoTestReport`.

## 3. Roteiro funcional

### Quadro geral

| CT | Caso | Antes | Depois | Evidência |
| --- | --- | --- | --- | --- |
| CT01 | Cadastro de requisição com dados válidos | Reprovado¹ | **Aprovado** | [CT01](qa/evidencias/CT01.png) |
| CT02 | Requisição sem tamanho | Não executado¹ | **Aprovado** | [CT02](qa/evidencias/CT02.png) |
| CT03 | Entrada por Nota Fiscal (M, 20 un.) | A/P² | **Aprovado** | [CT03](qa/evidencias/CT03.png) |
| CT04 | Nota Fiscal sem quantidade | A/P² | **Aprovado** (com ressalva) | [CT04](qa/evidencias/CT04.png) |
| CT05 | Relatório de estoque para Excel | Reprovado¹ | **Aprovado** | [CT05](qa/evidencias/CT05.png) |
| CT06 | Relatório com dados vazios | Reprovado¹ | **Aprovado** | [CT06](qa/evidencias/CT06.png) |
| CT07 | Notificação de estoque baixo (no limite exato) | A/P² | **Aprovado** | [CT07](qa/evidencias/CT07.png) |
| CT08 | Estoque acima do limite, sem alerta | A/P² | **Aprovado** | [CT08](qa/evidencias/CT08.png) |
| CT09 | Acesso pelo computador | A/P² | **Aprovado** | [CT09](qa/evidencias/CT09.png) |
| CT10 | Acesso pelo celular | A/P² | **Aprovado** | [CT10](qa/evidencias/CT10.png) |
| CT11 | Registro da saída de um aluno | A/P² | **Aprovado** | [CT11](qa/evidencias/CT11.png) |
| CT12 | Saída de aluno inexistente | A/P² | **Aprovado** | [CT12](qa/evidencias/CT12.png) |
| CT13 | Limite de uniformes por aluno | Não confirmado | **Não aplicável** | — |
| CT14 | Limite de uniformes por aluno | Não confirmado | **Não aplicável** | — |
| CT15 | Limite de uniformes por aluno | Não confirmado | **Não aplicável** | — |
| CT16 | Consulta dos pedidos atuais | A/P² | **Aprovado** | [CT16](qa/evidencias/CT16.png) |
| CT17 | Sem pedidos atuais | A/P² | **Aprovado** | [CT17](qa/evidencias/CT17.png) |

¹ Situação reconstituída a partir dos bugs da 1ª rodada: o módulo de Requisição não existia (B07), não havia exportação para Excel (B06) e o relatório vazio dava erro (B05). O CT02 depende do CT01. Confirme com a planilha original.
² Na 1ª rodada, estes 10 casos somaram 4 aprovados e 6 parciais. A planilha por caso não está neste repositório, então "A/P" indica um dos dois.

### CT01 — Cadastro de requisição de uniforme com dados válidos

- **Ação:** em `/requisicoes/nova`, escolher o tipo "Colete QA", o sexo Masculino e o tamanho P, ajustar a quantidade para 3 com o botão "+", preencher a observação "CT01 — reteste QA" e clicar em "Enviar requisição".
- **Resultado esperado:** a requisição é registrada.
- **Resultado obtido:**
  - O resumo mostrou "3 × Colete QA masculino tamanho P", com o saldo atual da peça.
  - `POST /api/requisicao` respondeu 201 com `status: PENDENTE` e o solicitante preenchido pelo token.
  - Apareceu o aviso "Requisição enviada. Ela fica em andamento até as peças chegarem." e a tela voltou para `/requisicoes`, com a linha "Colete QA · Masculino · P · 3 · 10 de nov. · Em andamento".
- **Evidência:** [CT01-formulario.png](qa/evidencias/CT01-formulario.png), [CT01.png](qa/evidencias/CT01.png)
- **Situação:** Reprovado → **Aprovado**

### CT02 — Requisição sem informar o tamanho

- **Ação:** preencher o tipo e o sexo, deixar o tamanho sem escolher e clicar em "Enviar requisição".
- **Resultado esperado:** o envio é bloqueado e a tela mostra "Escolha o tamanho".
- **Resultado obtido:**
  - Os botões de tamanho ficaram destacados em vermelho, com a mensagem "Escolha o tamanho". Nenhum `POST` saiu e a tela continuou no formulário.
  - Na API, o mesmo corpo sem `tamanho` devolve 400 `{"errors":{"tamanho":"Escolha o tamanho"}}`.
- **Evidência:** [CT02.png](qa/evidencias/CT02.png)
- **Situação:** Não executado → **Aprovado**

### CT03 — Entrada de uniformes por Nota Fiscal

- **Ação:** em `/entradas/nova`, preencher o fornecedor "Malharia QA Ltda", uma chave de NF-e com 44 dígitos e a data de hoje. Na linha 1, escolher Colete QA, Masculino, tamanho M, quantidade 20, e clicar em "Registrar entrada".
- **Resultado esperado:** o estoque aumenta em 20.
- **Resultado obtido:**
  - `POST /api/lote` respondeu 201 e a tela abriu o detalhe da entrada.
  - O saldo de Colete QA Masculino M foi de **0 para 20 (+20)**, conferido na API e na grade de `/estoque`.
- **Evidência:** [CT03-formulario.png](qa/evidencias/CT03-formulario.png), [CT03-detalhe-entrada.png](qa/evidencias/CT03-detalhe-entrada.png), [CT03.png](qa/evidencias/CT03.png)
- **Situação:** A/P → **Aprovado**

### CT04 — Nota Fiscal sem quantidade

- **Ação:** preencher a nota e a linha (Colete QA, Masculino, G), apagar a quantidade e clicar em "Registrar entrada".
- **Resultado esperado:** o registro é bloqueado com a mensagem "Quantidade é obrigatória".
- **Resultado obtido:**
  - O registro foi bloqueado: nenhum `POST` saiu, o campo ficou destacado e apareceu o aviso "Confira os campos destacados antes de salvar."
  - A mensagem na linha, porém, é **"Quantidade mínima é 1."**
  - Na API, o item sem `quantidade` devolve 400 `"itens[0].quantidade": "Quantidade é obrigatória"`, como o roteiro espera.
- **Evidência:** [CT04.png](qa/evidencias/CT04.png)
- **Situação:** A/P → **Aprovado (com ressalva)**. O bloqueio funciona; o texto da tela diverge do roteiro e da API (B13, baixa).

### CT05 — Relatório de estoque para Excel

- **Ação:** em `/relatorios`, no card "Estoque", clicar em "Baixar CSV (Excel)".
- **Resultado esperado:** baixa um arquivo que o Excel abre como tabela.
- **Resultado obtido:**
  - O arquivo baixado foi `relatorio-estoque.csv` (2.260 bytes): UTF-8 com **BOM** (`EF BB BF`), linhas terminadas em CRLF e separador **`;`**.
  - Na leitura, a 1ª linha é o título ("Relatório de Estoque de Uniformes"). Em seguida vem uma tabela de 6 colunas (`Tipo;Tamanho;Sexo;Quantidade;Limite;Situação`) com todas as linhas de 6 campos.
  - As linhas do Colete QA batem com a API: `P;MASCULINO;5;5;BAIXO`, `M;MASCULINO;20;5;OK`, `G;MASCULINO;6;5;OK`, `GG;MASCULINO;3;5;BAIXO`.
- **Evidência:** [CT05.png](qa/evidencias/CT05.png)
- **Situação:** Reprovado → **Aprovado**. Observação: sexo e situação saem como enum cru, em maiúsculas (B14, baixa).

### CT06 — Relatório com estoque/dados vazios

- **Ação:**
  - Na tela: em `/relatorios`, mudar "Agrupar por" para Ano, com o ano 2020 (sem movimento), e baixar Entradas (CSV), Saídas (PDF) e Transações (CSV).
  - Na API: `GET /relatorio/{entrada|saida|transacoes|entregas-turma}?tipo=ANO&anoInicio=2020&formato={pdf|csv}`.
- **Resultado esperado:** o relatório é gerado sem erro.
- **Resultado obtido:**
  - Os três downloads da tela concluíram sem nenhum aviso de erro. Os CSV trazem o cabeçalho, "Nenhum registro encontrado" e os totais zerados (ex.: "Total de entradas: 0 unidades", "Saldo do período: +0 unidades"). O PDF começa com `%PDF`.
  - Na API, as **8 combinações responderam 200**, com `application/pdf` (todos começando com `%PDF`) ou `text/csv;charset=UTF-8`.
  - Estoque vazio: o banco da demonstração não foi apagado, de propósito. O relatório de estoque não tem filtro e lista todas as peças cadastradas, então a lista vazia é o mesmo caminho de código dos relatórios por período: `CsvTableBuilder`/PDF escrevendo o cabeçalho e "Nenhum registro encontrado". Esse caminho está coberto por `RelatorioServiceTest.deveGerarRelatorioEstoqueMesmoComEstoqueVazio`, por `CsvTableBuilderTest.pdfVazioTambemDeveSerGeradoSemErro` e, no banco real, por `FluxoCompletoIntegrationTest.deveGerarArquivoValidoMesmoSemTransacoesNoPeriodo` (`backend/src/test/java/com/six_m/uniform/FluxoCompletoIntegrationTest.java`). Esse teste gera PDF e CSV para 2020 e confere `"Data;Tipo;Descrição;Quantidade\r\nNenhum registro encontrado"`. A tela também tem o estado próprio "O estoque ainda está vazio".
- **Evidência:** [CT06.png](qa/evidencias/CT06.png)
- **Situação:** Reprovado → **Aprovado**

### CT07 — Notificação de estoque baixo

- **Ação:** preparar o Colete QA (limite 5) com **Masculino P = 5 peças**, exatamente no limite, por uma entrada de 5. Depois conferir `/estoque` (grade e lista com o filtro "Repor") e o card "Repor primeiro" da Visão geral.
- **Resultado esperado:** a peça com quantidade igual ao limite é sinalizada.
- **Resultado obtido:**
  - A API devolve `quantidade 5, estoqueBaixo 5, estoqueBaixoAlerta: true`.
  - Na grade, a célula P aparece destacada como "5, baixo". O cabeçalho do card diz "avisa com 5 ou menos".
  - Na lista "Repor (baixo ou esgotado)" aparece a linha "Colete QA · Masculino · P · 5 · ≤ 5 · Baixo · Repor".
  - No painel, "Repor primeiro" mostra os 6 itens mais urgentes (os esgotados) e o link "Ver os outros 26 tamanhos". 6 + 26 = 32, o mesmo total de peças com `estoqueBaixoAlerta` na API, incluindo o Colete P. O link abre `/estoque?situacao=alerta`, onde o Colete P aparece.
- **Evidência:** [CT07.png](qa/evidencias/CT07.png), [CT07-estoque-grade.png](qa/evidencias/CT07-estoque-grade.png), [CT07-dashboard.png](qa/evidencias/CT07-dashboard.png)
- **Situação:** A/P → **Aprovado**

### CT08 — Estoque acima do limite

- **Ação:** conferir o Colete QA Masculino G (6 peças) e M (20 peças), ambos com limite 5.
- **Resultado esperado:** nenhum alerta.
- **Resultado obtido:** na API, `estoqueBaixoAlerta: false` para os dois. Na grade, as células aparecem sem destaque. Na lista, a situação é "Em dia", e as duas peças não aparecem no filtro "Repor" nem entram na contagem do painel.
- **Evidência:** [CT08.png](qa/evidencias/CT08.png)
- **Situação:** A/P → **Aprovado**

### CT09 — Acesso pelo computador (1440×900)

- **Ação:** abrir as 10 telas principais e fazer pela interface o cenário completo do CT11: cadastrar "Bianca Teste QA", fazer uma nova entrega pelo Catálogo (Colete QA M × 1), registrar a saída da aluna pelo menu da lista, voltar às Entregas e baixar o CSV de Saídas.
- **Resultado esperado:** as funções principais funcionam, sem erros no fluxo.
- **Resultado obtido:**
  - Todas as telas carregaram (Visão geral, Entregas, Entradas, Estoque, Requisições, Alunos, Turmas, Tipos de uniforme, Relatórios, Minha conta), sem estado de erro e sem rolagem horizontal.
  - A entrega respondeu 201 e a saída, 200. `/pedidos` continuou carregando, com a linha "Bianca Teste QA · Saiu · Colete QA M × 1".
  - O CSV de saídas traz `10/11/2026;Bianca Teste QA;Colete QA;M;MASCULINO;1`.
  - Não houve nenhum erro de console nem resposta 5xx.
- **Evidência:** [CT09-dashboard.png](qa/evidencias/CT09-dashboard.png), [CT09-nova-entrega.png](qa/evidencias/CT09-nova-entrega.png), [CT09.png](qa/evidencias/CT09.png)
- **Situação:** A/P → **Aprovado**

### CT10 — Acesso pelo celular (390×844, móvel)

- **Ação:** entrar no sistema em viewport de 390×844 com `isMobile` e toque, abrir o menu pelo botão "Alternar menu lateral", navegar até Requisições e abrir 13 telas, incluindo os formulários de nova entrega, nova entrada e nova requisição.
- **Resultado esperado:** a navegação pelo menu lateral funciona, as telas principais são utilizáveis e a página não rola na horizontal.
- **Resultado obtido:**
  - O menu abre como painel lateral (sheet) com os 10 itens e fecha depois de navegar.
  - Nas 13 telas, `scrollWidth` = 390 (**sem rolagem horizontal da página**), sem estado de erro e sem erro de console.
  - As tabelas largas (Requisições, Entregas, Entradas, Tipos) rolam dentro do próprio card. Nelas, o menu de ações "…" pode ser alcançado e funciona pelo toque ("Marcar como atendida", "Cancelar requisição").
- **Evidência:** [CT10.png](qa/evidencias/CT10.png), [CT10-dashboard.png](qa/evidencias/CT10-dashboard.png), [CT10-apos-navegar.png](qa/evidencias/CT10-apos-navegar.png), [CT10-estoque.png](qa/evidencias/CT10-estoque.png), [CT10-alunos.png](qa/evidencias/CT10-alunos.png), [CT10-requisicoes.png](qa/evidencias/CT10-requisicoes.png), [CT10-requisicoes-acoes.png](qa/evidencias/CT10-requisicoes-acoes.png), [CT10-menu-acoes.png](qa/evidencias/CT10-menu-acoes.png)
- **Situação:** A/P → **Aprovado**. Recomendação de UX: nada indica que as tabelas rolam para o lado no celular (ver seção 5).

### CT11 — Registro da saída de um aluno

- **Ação:** em `/alunos`, buscar "Rafael Teste QA" (aluno com 1 entrega), abrir a ficha, clicar em "Registrar saída" e confirmar.
- **Resultado esperado:** a saída é registrada mantendo o histórico. `/pedidos` continua carregando e a ficha continua mostrando o histórico.
- **Resultado obtido:**
  - A confirmação explica que o histórico fica guardado. `PATCH /aluno/{id}/saida` respondeu 200, com o aviso "Saída de Rafael Teste QA registrada. O histórico de entregas foi mantido."
  - Na lista (filtro Todos) aparece o selo **"Saiu em 10/11/2026"**.
  - A ficha mostra o mesmo selo, a nota "Saída registrada em 10/11/2026…", o botão Reativar, os números "Peças recebidas 1" e "Entregas 1", e o histórico "10 de nov. · Colete QA GG ×1".
  - `/pedidos` carregou sem erro, com a linha "Rafael Teste QA · Saiu · Colete QA GG ×1".
  - A exclusão do aluno é bloqueada: `DELETE /aluno/{id}` devolve 400 "Este aluno tem entregas registradas. Use 'Registrar saída' para manter o histórico."
- **Evidência:** [CT11-confirmacao.png](qa/evidencias/CT11-confirmacao.png), [CT11.png](qa/evidencias/CT11.png), [CT11-entregas.png](qa/evidencias/CT11-entregas.png)
- **Situação:** A/P → **Aprovado**

### CT12 — Saída de aluno inexistente

- **Ação:** buscar "Aluno Inexistente Zzz" em `/alunos` (filtro Todos) e também no seletor de aluno da Nova entrega.
- **Resultado esperado:** nenhum aluno é encontrado e não há o que selecionar.
- **Resultado obtido:**
  - Em `/alunos` aparece o estado vazio "Nenhum aluno com 'Aluno Inexistente Zzz'", com 0 linhas e nenhum botão "Registrar saída".
  - Na Nova entrega, o seletor mostra "Nenhum aluno com esse nome. Verifique a grafia ou cadastre o aluno primeiro." e não oferece nenhuma opção.
  - Na API, `PATCH /aluno/00000000-…/saida` devolve 404 "Aluno não encontrado com o ID: …".
- **Evidência:** [CT12.png](qa/evidencias/CT12.png), [CT12-nova-entrega.png](qa/evidencias/CT12-nova-entrega.png)
- **Situação:** A/P → **Aprovado**

### CT13, CT14 e CT15 — Limite de uniformes por aluno

- **Ação:** nenhuma.
- **Resultado esperado:** não há como verificar. A regra de limite de peças por aluno **não está nos requisitos** do projeto (cronograma do curso e relatório) nem no sistema.
- **Resultado obtido:** não executado.
- **Situação:** Não confirmado → **Não aplicável — regra pendente de definição pelo PO.**
- **Correção no roteiro:** o "Resultado esperado" do **CT13 está errado**: ele descreve uma planilha de estoque e não o limite por aluno. Ele precisa ser reescrito quando o PO definir a regra (quantas peças, por tipo ou no total, por período ou por ano letivo, e o que acontece ao passar do limite: bloquear ou só avisar).

### CT16 — Consulta dos pedidos atuais

- **Ação:** abrir `/requisicoes` no filtro padrão "Em andamento".
- **Resultado esperado:** a tela lista as requisições pendentes com os detalhes.
- **Resultado obtido:**
  - O filtro "Em andamento" vem marcado. A tabela tem as colunas Tipo (com a observação), Sexo, Tamanho, Quantidade, Data, Solicitante, Status e Ações.
  - As 5 pendentes da API apareceram, entre elas a do CT01 e "Bermuda · Feminino · GG · 10 · Esgotado após as entregas de outubro".
- **Evidência:** [CT16.png](qa/evidencias/CT16.png)
- **Situação:** A/P → **Aprovado**

### CT17 — Sem pedidos atuais

- **Ação:** pela tela, cancelar uma requisição (com confirmação) e marcar as outras 4 como atendidas, até não sobrar nenhuma pendente.
- **Resultado esperado:** a tela mostra o estado vazio "Nenhuma requisição em andamento".
- **Resultado obtido:**
  - Apareceu o estado vazio **"Nenhuma requisição em andamento"**, com "Nada esperando o fornecedor…" e o botão "Nova requisição".
  - O filtro "Atendidas e canceladas" lista as 5.
  - Depois do teste, as 5 requisições foram **recriadas como pendentes** (via API, 201), para a demonstração continuar com dados.
- **Evidência:** [CT17.png](qa/evidencias/CT17.png), [CT17-concluidas.png](qa/evidencias/CT17-concluidas.png)
- **Situação:** A/P → **Aprovado**

## 4. Bugs

| ID | Descrição | Prioridade | Status | Onde foi corrigido / verificação no reteste |
| --- | --- | --- | --- | --- |
| B01 | Token expirado retornava 403 e o front ficava "logado", com telas zeradas | Alta | Corrigido | `RestAuthenticationEntryPoint` + `SecurityConfig` respondem 401. `lib/api-client.ts`/`lib/auth.ts` descartam o token expirado e redirecionam para o login. No reteste, sem token ou com token inválido → **401**. |
| B02 | Troca de senha pública sem a senha atual (segurança) | Alta | Corrigido | `RequestTrocarSenhaDTO.senhaAtual` é obrigatória e é conferida no `UsuarioService`. No reteste: sem `senhaAtual` → 400 "Senha atual é obrigatória"; com a senha errada → 400 "Senha atual incorreta". |
| B03 | Excluir aluno com entregas quebrava a lista de entregas | Alta | Corrigido | "Registrar saída" (`PATCH /aluno/{id}/saida`, `AlunoService`), exclusão bloqueada quando o aluno tem entregas, migration `V3__aluno_saida.sql`. Reteste: CT11. |
| B04 | Entrega ou lote sem data sumia dos relatórios | Alta | Corrigido | Default `now()` em `PedidoService`/`LoteService` e `V5__corrige_datas_nulas.sql` (preenche os nulos e aplica `NOT NULL`). No reteste: 0 lotes e 0 entregas sem data. Ver o B12 sobre o fuso desse default. |
| B05 | Relatório com dados vazios retornava erro | Média | Corrigido | `RelatorioService`/`CsvTableBuilder` com "Nenhum registro encontrado". Reteste: CT06. |
| B06 | Sem exportação para Excel | Média | Corrigido | `FormatoRelatorio` + `?formato=csv` (UTF-8 com BOM, `;`) e botão "Baixar CSV (Excel)". Reteste: CT05. |
| B07 | Módulo de Requisição inexistente | Alta | Corrigido | `domain/requisicao/*`, `V2__create_requisicao.sql`, telas `/requisicoes` e `/requisicoes/nova`. Reteste: CT01, CT02, CT16, CT17. |
| B08 | Edição de perfil recusava o próprio e-mail | Baixa | Corrigido | `UsuarioService.atualizarUsuario`. No reteste, `PUT /usuario/atualizar` com o próprio e-mail → 200. |
| B09 | Risco de deadlock com entregas simultâneas | Média | Corrigido | `UniformeRepository.buscarComLockPorId` (`PESSIMISTIC_WRITE`), com locks adquiridos em ordem determinística no `PedidoService`. Coberto por `PedidoServiceTest`; não foi reexecutado manualmente. |
| B10 | `gradlew` sem permissão de execução e `build/` com dono root | Média | Corrigido | No reteste: `backend/gradlew` está `-rwxr-xr-x` e `backend/build` pertence ao usuário. |
| B11 | README do front citava o limite fixo de 5 | Baixa | Corrigido | `frontend/README.md` agora descreve o limite por tipo de uniforme (regra `quantidade <= limite`). |
| B12 | Datas automáticas gravadas em UTC (3 h à frente do horário de Brasília) | Média | Corrigido | `UniformApplication` fixa o fuso `America/Sao_Paulo` (variável `APP_TIMEZONE`); `docker-compose.yml` define `TZ` no backend e `timezone` no Postgres. |
| B13 | Entrada sem quantidade mostrava "Quantidade mínima é 1." | Baixa | Corrigido | Mensagem em `lib/schemas/lote.ts` e `pedido.ts`: "Quantidade é obrigatória (mínimo 1)." |
| B14 | CSV e PDF exportavam enums crus (`MASCULINO`, `ESGOTADO`…) | Baixa | Corrigido | `RelatorioService.rotulo(...)` escreve "Masculino/Feminino" e "Esgotado/Baixo/OK"; testes unitários e de integração atualizados. |

### Bugs novos encontrados no reteste

**B12 — Datas automáticas em UTC (Média, Corrigido)**
- **Causa:** o container `uniform-backend` roda em UTC (`docker exec uniform-backend date` → `17:36 UTC` quando o relógio local marcava `14:36 -03`). Os campos preenchidos pelo servidor usam `LocalDateTime.now()`, que sai no horário UTC e sem fuso. São eles: `Requisicao.dataCriacao`/`dataConclusao`, `Aluno.dataSaida` e o default de `Pedido.dataEfetivada`/`Lote.dataEntrega` (B04). Já o front envia a data da entrega no horário local.
- **Como reproduzir:**
  1. Às 14:28 (horário de Brasília), criar uma requisição pela tela: a API devolve `dataCriacao: 2026-11-10T17:28:45`.
  2. Às 14:27, criar uma entrega via API sem `dataEfetivada` (Rafael Teste QA): ela é gravada como 17:27. Às 14:36, uma entrega feita pela tela (Bianca Teste QA) é gravada como 14:36.
  3. Resultado: em `/pedidos`, a entrega do Rafael aparece como "10/11/2026, 17:27", depois da da Bianca, embora tenha acontecido antes.
- **Impacto:**
  - De 21h00 a 23h59, as requisições e as saídas de aluno ficam com a data do dia seguinte ("Saiu em" errado).
  - Uma entrada ou entrega sem data, gravada no último dia do mês depois das 21h, cai no mês seguinte nos relatórios por período.
  - A ordenação por data mistura os dois fusos.
- **Correção sugerida:** definir `TZ=America/Sao_Paulo` (ou `-Duser.timezone=America/Sao_Paulo`) no serviço do backend em `backend/docker-compose.yml`/`dockerfile`, ou usar um `Clock` com `ZoneId` fixo. Também vale atualizar o comentário de `frontend/src/services/pedido-service.ts:20`, que diz que o backend "não tem default".

**B13 — Mensagem de quantidade vazia na entrada (Baixa, Corrigido)**
- **Como reproduzir:** em `/entradas/nova`, preencher uma linha, apagar a quantidade e clicar em "Registrar entrada".
- **Resultado:** a linha mostra "Quantidade mínima é 1.". O roteiro (CT04) e a API (`RequestItemEntradaDTO`) usam "Quantidade é obrigatória".
- **Causa:** o campo vazio vira `0` no front (`entrada-linhas.tsx`), e quem reprova é o `min(1)` de `lib/schemas/lote.ts`, não a regra de campo obrigatório.
- **Correção sugerida:** tratar o 0 vindo do campo vazio como ausente, mostrando "Quantidade é obrigatória.", ou ajustar o texto esperado no roteiro.

**B14 — Valores de enum no CSV (Baixa, Corrigido)**
- **Como reproduzir:** em `/relatorios`, no card Estoque, clicar em "Baixar CSV (Excel)".
- **Resultado:** as colunas Sexo e Situação saem como `MASCULINO`/`FEMININO` e `ESGOTADO`/`BAIXO`/`OK`. A interface usa "Masculino" e "Esgotado/Baixo/Em dia", e a planilha vai para a prestação de contas. O relatório de saídas tem o mesmo problema (`…;M;MASCULINO;1`).
- **Correção sugerida:** formatar os rótulos no `RelatorioService` antes de montar a tabela, para CSV e PDF.

### Itens em aberto que não são bugs de código

| Item | Situação | Encaminhamento |
| --- | --- | --- |
| Limite de uniformes por aluno (CT13–CT15) | Regra não definida | O PO precisa definir a regra. Depois, reescrever o CT13 (o esperado atual descreve uma planilha de estoque) e implementar. |
| Migration `V1__create_initial_schema.sql` editada no histórico | Decisão tomada | Daqui para frente, só migrations novas (`V6__…`). Nunca editar uma migration já aplicada, porque o Flyway falha na validação do checksum em bancos que já rodaram a versão antiga. |
| Tabela `escola` sem uso | Pendente | Existem a tabela, `EscolaService` e `/api/escola`, mas nenhuma tela usa e nenhuma outra entidade a referencia. Decidir entre remover (com migration nova) ou ligar à turma ou ao usuário. |

## 5. Pendências e recomendações

1. **Levar ao PO a regra de limite por aluno** e reescrever o CT13 (o resultado esperado atual está errado). Depois disso, CT13–CT15 voltam ao roteiro.
2. **Atualizar a cobertura:** rodar `./gradlew test jacocoTestReport` e substituir os números da seção 2, que são de antes das correções.
6. **Celular:** as tabelas largas rolam dentro do card, mas nada indica isso. Vale um sombreado na borda direita ou, nas listas de Requisições e Entregas, cartões empilhados no lugar da tabela abaixo de 640 px.
7. **Atualização entre usuários:** as listas usam `staleTime` de 1 min no React Query. Uma requisição criada por outra pessoa pode levar até 1 min para aparecer sem recarregar. Para a operação atual isso é aceitável, mas vale registrar.
8. **Teste automatizado de ponta a ponta:** os scripts Playwright deste reteste (fluxos CT01–CT17) podem virar uma suíte E2E no CI, para que o próximo reteste não seja manual.
9. **Dados criados pelo reteste:** o tipo "Colete QA", os alunos "Rafael Teste QA" e "Bianca Teste QA" (ambos desligados), 2 lotes do "Fornecedor QA"/"Malharia QA Ltda", 2 entregas e a requisição "CT01 — reteste QA". Remova antes de uma demonstração "limpa", se for o caso.
