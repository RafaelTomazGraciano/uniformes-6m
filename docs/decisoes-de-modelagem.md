# Documento de decisões de modelagem

Este documento registra as principais decisões de modelagem de dados e de arquitetura do sistema 6M Uniformes. Cada decisão segue o formato **contexto → decisão → alternativas consideradas → consequências**. Tudo o que está descrito aqui foi conferido no código do repositório (backend em `backend/src/main/java/com/six_m/uniform/`, schema nas migrations `backend/src/main/resources/db/migration/V1__…` a `V5__…`) e no histórico do Git.

Documentos relacionados: [ERD.md](./ERD.md) (diagrama e dicionário de dados) e [ddl.sql](./ddl.sql) (DDL completo).

---

## Parte 1 — Decisões de modelagem de dados

### D1. Chaves primárias UUID geradas pelo banco

- **Contexto.** Todas as entidades precisam de um identificador exposto na API REST (`/api/aluno/{id}`, `/api/pedido/{id}` etc.).
- **Decisão.** Todas as 12 tabelas usam `id uuid PRIMARY KEY DEFAULT (gen_random_uuid())`. No JPA as entidades usam `@GeneratedValue(strategy = GenerationType.UUID)`, então o Hibernate gera o UUID antes do `INSERT`; o `DEFAULT` do banco serve para inserções feitas fora da aplicação.
- **Alternativas consideradas.** `bigserial`/`IDENTITY` (sequencial).
- **Consequências.** (+) IDs não são adivinháveis nem revelam volume de registros pela URL; podem ser gerados sem ida ao banco. (−) 16 bytes por chave e índices maiores que os de inteiros; IDs menos legíveis em depuração. Para o volume de uma escola, o custo é desprezível.

### D2. Enums nativos do PostgreSQL para domínios fechados

- **Contexto.** Vários atributos têm um conjunto pequeno e fixo de valores: sexo, tamanho, turno, etapa de ensino e tipo de escola.
- **Decisão.** Criar tipos `ENUM` no banco (`sexo`, `tamanho`, `tipo_escola`, `turno`, `ensino` e, desde a V2, `status_requisicao`) espelhados por enums Java em `shared/enums/`. As entidades mapeiam com `@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` (em `Uniforme`, `ItemLote` e `Turma`).
- **Alternativas consideradas.** (a) `varchar` com `CHECK (... IN (...))`; (b) tabelas de domínio com FK; (c) `ordinal` inteiro.
- **Consequências.** (+) O banco rejeita valores inválidos e a ordem de `tamanho` (`PP < P < M < G < GG`) é a ordem natural de ordenação do enum. (−) Acrescentar um valor exige migration — aconteceu com `turno` ao receber `INTEGRAL` no commit `43ed70d`. Observação: `Escola` usa só `@Enumerated(EnumType.STRING)`, sem `NAMED_ENUM`; como a escola é apenas lida pela API, isso ainda não causou problema, mas destoa das demais entidades.

### D3. Versionamento do schema com Flyway e validação pelo Hibernate

- **Contexto.** O time precisava de um schema reproduzível em qualquer máquina, no Docker e nos testes de integração (Testcontainers).
- **Decisão.** O schema vive em migrations Flyway (`V1__create_initial_schema.sql` a `V5__corrige_datas_nulas.sql`), executadas na subida da aplicação (`spring.flyway.enabled=true`). O Hibernate roda com `spring.jpa.hibernate.ddl-auto=validate`: ele **não** cria tabelas, só confere se as entidades batem com o banco.
- **Alternativas consideradas.** `ddl-auto=update` (o Hibernate altera o schema sozinho); scripts SQL manuais.
- **Consequências.** (+) O DDL é explícito, revisável em PR e fonte única da verdade; divergência entre entidade e tabela derruba a aplicação na inicialização, e não em produção. (−) Durante o desenvolvimento o time optou por **editar a própria V1** em vez de criar V2, V3… (o arquivo foi alterado nos commits `d6a3bea`, `5aa4f0d`, `43ed70d` e `1063a46`). Isso manteve um único script limpo, mas um banco criado com uma versão anterior falha na validação de checksum do Flyway e precisa ser recriado (ex.: apagando o volume `uniform_data` do Docker). **A partir desta rodada a regra mudou**: a V1 ficou congelada e toda mudança entra como uma versão nova (`V2__create_requisicao`, `V3__aluno_saida`, `V4__view_estoque`, `V5__corrige_datas_nulas`). Assim bancos existentes (inclusive o de demonstração) são atualizados sem perder dados, e migrations de dados (como a recuperação de alunos na V3 e o preenchimento de datas na V5) ficam registradas. Ver D22.

### D4. Uniforme como SKU (tipo, tamanho, sexo) que guarda o saldo de estoque

- **Contexto.** A escola precisa saber quantas peças de cada variação existem (ex.: "Camiseta M feminina"), e as entregas debitam dessas variações.
- **Decisão.** Separar o catálogo (`tipo_uniforme`) do saldo (`uniforme`). Cada linha de `uniforme` é uma variação única, garantida por `CREATE UNIQUE INDEX uq_uniforme_tipo_tamanho_sexo ON uniforme (tipo_id, tamanho, sexo)`, e tem uma coluna `quantidade` com o saldo atual (`CHECK (quantidade >= 0)`). As linhas de `uniforme` não são cadastradas manualmente: a API de uniforme só tem `GET`, e o registro é criado na primeira entrada de lote daquela variação (`UniformeService.darEntrada` usa `findByTipoUniformeIdAndTamanhoAndSexo(...).orElseGet(...)`).
- **Alternativas consideradas.** (a) Uma tabela única com tipo/tamanho/sexo em texto livre; (b) não guardar saldo e calculá-lo sempre como Σ entradas − Σ saídas.
- **Consequências.** (+) Leitura do estoque é imediata (uma linha por SKU), e o banco impede saldo negativo e SKUs duplicados. (−) O saldo é um dado derivado/redundante: precisa ser mantido em sincronia com lotes e pedidos pela camada de serviço (ver D7). Histórico: até o commit `43ed70d`, `uniforme` também tinha `deletado`, removido porque o SKU nasce e é mantido automaticamente.

### D5. Limite de estoque baixo por tipo de uniforme

- **Contexto.** O relatório e a tela de estoque precisavam sinalizar peças acabando. Itens diferentes têm giros diferentes (ex.: camiseta vs. agasalho), então um limite global não serve.
- **Decisão.** Commit `1063a46` (#20) adicionou `tipo_uniforme.estoque_baixo int NOT NULL`. A regra fica no domínio: `Uniforme.estaComEstoqueBaixo()` retorna `quantidade <= tipoUniforme.getEstoqueBaixo()`. O valor é obrigatório e não negativo na API (`@NotNull @PositiveOrZero` nos DTOs de tipo de uniforme), é devolvido em `ResponseUniformeDTO` e usado na situação de estoque (`ESGOTADO`/`BAIXO`/`OK`), hoje calculada pela VIEW `vw_estoque` (ver D19) para o painel e o relatório. O frontend aplica a mesma regra em `lib/estoque.ts`; não existe mais limite fixo (o antigo "5 peças") em nenhum lugar.
- **Alternativas consideradas.** (a) Limite fixo no código ou em configuração; (b) limite por SKU (coluna em `uniforme`); (c) limite só no frontend.
- **Consequências.** (+) Um único número por tipo é fácil de manter pelo usuário e vale para todos os tamanhos/sexos daquele tipo. (−) Não permite limites diferentes por tamanho (ex.: tamanho M sai mais que PP). O `CHECK >= 0` está apenas na API, não no banco.

### D6. Entradas modeladas como NotaFiscal → Lote → ItemLote

- **Contexto.** As peças chegam do fornecedor em remessas acompanhadas de nota fiscal; para prestação de contas é preciso saber o que chegou, de quem, quando e com qual NF.
- **Decisão.** Três tabelas: `nota_fiscal` (chave de acesso), `lote` (fornecedor, `data_entrega`, FK para a nota) e `item_lote` (tipo, tamanho, sexo e quantidade, FK para o lote). O `item_lote` referencia `tipo_uniforme` + tamanho + sexo, **e não** `uniforme`, porque o SKU pode ainda não existir quando a primeira remessa chega. O lote é criado e editado em uma única operação transacional (`LoteService.criarLote`/`atualizarLote`), que também cria/atualiza a nota.
- **Alternativas consideradas.** (a) Guardar a chave da NF como coluna de `lote`; (b) uma tabela "movimentação" única para entradas e saídas.
- **Consequências.** (+) Registro auditável de cada entrada, base dos relatórios de entrada e de transações. (−) O schema permite **vários lotes por nota** (`lote.nota_fiscal_id` não é `UNIQUE`), mas a aplicação sempre cria uma nota nova por lote (1:1 na prática). A unicidade de `chave_acesso` é garantida apenas no `NotaFiscalService` (`existsByChaveAcesso`), sem `UNIQUE` no banco. Não há endpoint para excluir lotes nem `nota_fiscal` (apenas `GET`).

### D7. Atualização do estoque feita na camada de serviço (sem triggers)

- **Contexto.** Toda entrada (lote) soma no saldo e toda entrega (pedido) subtrai; edições precisam desfazer o efeito anterior.
- **Decisão.** Não há triggers nem procedures no banco. O saldo é atualizado pelo `UniformeService` dentro da mesma transação (`@Transactional`) que grava o lote ou o pedido:
  - `darEntrada` (lote criado) e `estornarEntrada` (lote editado — os itens antigos são estornados e apagados, e os novos lançados);
  - `darSaida` (pedido criado) e `estornarSaida` (pedido editado — mesma estratégia de estornar e relançar).
  As leituras para escrita usam **lock pessimista**, adquirido em ordem determinística (ver D21), (`@Lock(LockModeType.PESSIMISTIC_WRITE)`, ou seja, `SELECT ... FOR UPDATE`) em `UniformeRepository`. `darSaida` recusa saídas maiores que o saldo, `estornarEntrada` recusa estornos que deixariam o saldo negativo (unidades já entregues), e o `CHECK (quantidade >= 0)` funciona como última barreira.
- **Alternativas consideradas.** (a) Triggers `AFTER INSERT/UPDATE/DELETE` em `item_lote` e `pedido_uniforme`; (b) não guardar saldo e calcular por agregação.
- **Consequências.** (+) A regra fica em Java, legível, coberta por testes unitários (`UniformeServiceTest`, `LoteServiceTest`, `PedidoServiceTest`) e pelo teste de integração `FluxoCompletoIntegrationTest`, com mensagens de erro amigáveis. O lock evita que duas entregas simultâneas vendam a mesma peça duas vezes. (−) Qualquer escrita direta no banco (fora da API) deixa o saldo inconsistente; todo novo fluxo que movimente estoque precisa passar pelo `UniformeService`.

### D8. Entregas modeladas como Pedido → PedidoUniforme

- **Contexto.** Um aluno pode receber várias peças em uma mesma entrega, e é preciso saber quem registrou.
- **Decisão.** `pedido` é o cabeçalho (aluno, usuário, `data_efetivada`) e `pedido_uniforme` é a tabela associativa N:N entre `pedido` e `uniforme`, com atributo próprio `quantidade` (`CHECK > 0`) e PK substituta (`id` UUID). Diferentemente do item de lote, o item de pedido referencia diretamente o `uniforme` (SKU), pois só se entrega o que já existe em estoque. `pedido.usuario_id` passa a apontar para o usuário que editou por último (`PedidoService.atualizarPedido`).
- **Alternativas consideradas.** (a) Um pedido por peça (sem tabela de itens); (b) PK composta `(pedido_id, uniforme_id)`.
- **Consequências.** (+) Uma entrega com várias peças é uma única operação; os relatórios de saída e de entregas por turma navegam `pedido_uniforme → pedido → aluno → turma`. (−) Como não há PK composta nem `UNIQUE (pedido_id, uniforme_id)`, o banco permite o mesmo SKU duas vezes no mesmo pedido; a aplicação evita isso consolidando os itens repetidos antes de salvar (`PedidoUniformeService.consolidarItens`). Não há endpoint de exclusão de pedido, e não há registro do usuário original após edição.
- **Histórico.** O commit `d6a3bea` chegou a criar uma tabela `devolucao` (com enum `situacao_devolucao`: `BOM_ESTADO`, `DANIFICADO`, `PERDIDO`) ligada a `pedido_uniforme`, e o `uniforme` tinha uma coluna `devolvido`. Ambas foram removidas (`d6a3bea` removeu `devolvido`; `5aa4f0d` removeu `devolucao`), tirando devoluções do escopo.

### D9. `ChaveItemLote`: chave composta para consolidar itens de entrada

- **Contexto.** No formulário de entrada o usuário pode informar a mesma variação em mais de uma linha (ex.: duas linhas "Camiseta / M / FEMININO").
- **Decisão.** `ChaveItemLote` é um `record` Java (`tipoUniformeId, tamanho, sexo`) usado **apenas em memória** como chave de um `Map` em `ItemLoteService.consolidarItens`; as quantidades repetidas são somadas (com `Math.addExact`, que lança erro em caso de estouro de inteiro) antes de gravar um único `item_lote` por variação. Não é uma chave composta do banco — `item_lote` continua com PK UUID.
- **Alternativas consideradas.** (a) `@EmbeddedId`/`@IdClass` com PK composta em `item_lote`; (b) `UNIQUE (lote_id, tipo_uniforme_id, tamanho, sexo)` no banco; (c) gravar linhas duplicadas.
- **Consequências.** (+) Cada lote tem no máximo uma linha por variação, o que simplifica relatórios e estornos; o `record` dá `equals/hashCode` corretos de graça. (−) A regra existe só na aplicação: não há restrição de unicidade equivalente no banco. O pedido usa a mesma ideia com chave simples (`uniformeId`).

### D10. Exclusão lógica (soft delete) para pessoas e exclusão restrita para cadastros

- **Contexto.** Alunos e usuários aparecem no histórico de entregas (`pedido.aluno_id`, `pedido.usuario_id`); apagá-los fisicamente quebraria o histórico ou seria bloqueado pelas FKs.
- **Decisão.**
  - `aluno` e `usuario` têm `deletado bool NOT NULL DEFAULT false`. No aluno, a exclusão ficou restrita a cadastros feitos por engano: desde esta rodada a API recusa excluir aluno com entregas e oferece a **saída** (ver D18). As entidades usam `@SQLDelete(sql = "UPDATE ... SET deletado = true WHERE id = ?")` e `@SQLRestriction("deletado = false")`, então `repository.delete()` vira um `UPDATE` e consultas JPA ignoram os excluídos.
  - Ao excluir o usuário, o e-mail recebe o sufixo `|_deleted_|<id>` (`UsuarioService.deletarUsuario`) para liberar o `UNIQUE` de `email` e permitir novo cadastro com o mesmo endereço.
  - `turma` e `tipo_uniforme` são excluídos fisicamente, mas o serviço bloqueia a exclusão se houver dependentes (`TurmaService.deletarTurma` verifica alunos; `TipoUniformeService.deletarTipoUniforme` verifica uniformes e itens de lote), retornando 400 com mensagem clara. As FKs sem `ON DELETE` (padrão `NO ACTION`) garantem o mesmo no banco.
  - Lotes, notas fiscais, pedidos e uniformes não têm endpoint de exclusão.
- **Alternativas consideradas.** `ON DELETE CASCADE` (apagaria histórico); exclusão física em tudo; soft delete em todas as tabelas.
- **Consequências.** (+) O histórico de entregas e os relatórios continuam íntegros. (−) Os registros "excluídos" continuam ocupando espaço e exigem cuidado em consultas nativas (que não aplicam `@SQLRestriction`). Pior: o `@SQLRestriction` também vale nos *joins*, então um aluno excluído com entregas sumia do `pedido` que o referenciava — foi a causa do bug corrigido em D18. Os índices únicos de `turma`/`tipo_uniforme` não precisam considerar excluídos, pois esses são apagados de fato.

### D11. Unicidade sem diferenciar maiúsculas/minúsculas em nomes

- **Contexto.** Evitar cadastros duplicados como "Camiseta" e "camiseta", ou "1º A" e "1º a".
- **Decisão.** Índices únicos funcionais `uq_tipo_uniforme_tipo ON tipo_uniforme ((lower(tipo)))` e `uq_turma_nome ON turma ((lower(nome)))` (commit `43ed70d`). Os serviços também validam antes com `existsByTipoIgnoreCase`/`existsByNomeIgnoreCase` e aplicam `trim()`, para devolver mensagem amigável em vez de erro de constraint.
- **Alternativas consideradas.** `UNIQUE` simples (sensível a maiúsculas); tipo `citext`; validação só na aplicação.
- **Consequências.** (+) Dupla proteção (aplicação + banco). (−) Duas fontes da mesma regra para manter alinhadas.

### D12. Restrições `CHECK` de quantidade

- **Contexto.** Quantidades negativas ou zeradas não fazem sentido em itens de entrada/saída, e o saldo nunca pode ser negativo.
- **Decisão.** `ck_uniforme_quantidade CHECK (quantidade >= 0)`, `ck_item_lote_quantidade CHECK (quantidade > 0)` e `ck_pedido_uniforme_quantidade CHECK (quantidade > 0)` (commit `43ed70d`), além da validação nos DTOs.
- **Consequências.** (+) Proteção no banco mesmo se a regra da aplicação falhar. (−) `item_lote.quantidade` ficou com `DEFAULT 0`, que contradiz o `CHECK > 0` (resíduo da versão anterior do script); como a aplicação sempre informa a quantidade, isso não tem efeito prático, mas o `DEFAULT` poderia ser removido.

### D13. Dados de contato do responsável no aluno

- **Contexto.** A escola precisa contatar a família para retirada de uniformes.
- **Decisão.** Commit `43ed70d` acrescentou `aluno.nome_responsavel varchar(255)` e `aluno.telefone_responsavel varchar(11)`, ambos opcionais. O telefone é guardado só com dígitos (DDD + número); a API valida com `@Pattern("^(\d{10,11})?$")`.
- **Alternativas consideradas.** Tabela separada `responsavel` (N:N com aluno).
- **Consequências.** (+) Simples e suficiente para um contato por aluno. (−) Não suporta múltiplos responsáveis; se surgir essa necessidade, será preciso normalizar.

### D14. Entidade `escola` isolada

- **Contexto.** O modelo inicial previa o cadastro de escolas (com tipo: pública, técnica, parceira, cívico-militar, militar).
- **Decisão.** A tabela `escola` existe no schema e é exposta apenas para leitura (`GET /api/escola` e `GET /api/escola/{id}`); o DTO de criação foi removido no commit `cf4d1d7`. Nenhuma tabela tem FK para `escola` e a migration não insere dados nela.
- **Alternativas consideradas.** Relacionar `turma` (ou `usuario`) a `escola` para suportar várias escolas (multi-tenant).
- **Consequências.** (+) O sistema atende uma única escola sem a complexidade de isolar dados por escola. (−) A tabela hoje é efetivamente decorativa: para ter dados é preciso inseri-los diretamente no banco, e uma eventual evolução para várias escolas exigirá novas FKs e filtros em todas as consultas.

### D15. Datas como `timestamp` sem fuso e obrigatórias com default

- **Contexto.** Relatórios filtram entradas por `lote.data_entrega` e saídas por `pedido.data_efetivada`. Na V1 as duas colunas eram anuláveis e o backend gravava `null` quando o corpo não trazia data — esses registros sumiam de todos os relatórios por período.
- **Decisão.** Colunas `timestamp` (sem time zone), mapeadas para `LocalDateTime`. A migration `V5__corrige_datas_nulas.sql` preencheu as datas nulas com `now()` (as tabelas não têm data de criação, então é o melhor palpite) e tornou as duas colunas `NOT NULL DEFAULT now()`. Na aplicação, `PedidoService` e `LoteService` usam o momento do registro quando a data não vem, e uma edição sem data mantém a original em vez de apagá-la.
- **Alternativas consideradas.** Deixar anulável e tratar `null` nos relatórios; exigir a data no DTO (`@NotNull`).
- **Consequências.** (+) Nenhuma entrega ou entrada fica fora dos relatórios; a regra vale no banco e na API. (−) Registros antigos corrigidos pela V5 ficaram com a data da migração, não com a data real (não havia como recuperá-la). Fuso único continua sendo uma premissa.

### D16. Restrições de integridade `DEFERRABLE INITIALLY IMMEDIATE`

- **Contexto.** As FKs foram geradas a partir do dbdiagram.io, que emite essa cláusula.
- **Decisão.** Todas as 11 FKs (9 da V1 + 2 da V2) são `DEFERRABLE INITIALLY IMMEDIATE`: verificadas imediatamente por padrão, mas podem ser adiadas para o fim da transação com `SET CONSTRAINTS ... DEFERRED`.
- **Consequências.** Sem efeito no comportamento atual (a aplicação não adia constraints); deixa a porta aberta para cargas de dados em ordem arbitrária.

### D17. Requisição de novas peças com status e sem efeito no estoque

- **Contexto.** O painel de estoque mostrava o que estava acabando, mas não havia onde registrar "precisamos de 20 camisetas M femininas" — isso ficava em papel ou mensagem.
- **Decisão.** Tabela `requisicao` (V2) com tipo + tamanho + sexo + quantidade, observação, solicitante (`usuario_id`, vindo do token), datas de criação/conclusão e `status` (`PENDENTE` → `ATENDIDA` ou `CANCELADA`, enum `status_requisicao`). Só requisições pendentes mudam de status (`PATCH /{id}/atender` e `/{id}/cancelar`). A requisição **não mexe no estoque**: as peças entram quando o lote do fornecedor é registrado. Como o item de lote, ela referencia `tipo_uniforme` (e não `uniforme`), porque a variação pode ser algo que nunca entrou no estoque.
- **Alternativas consideradas.** (a) Atender a requisição dando entrada automática no estoque; (b) vincular a requisição ao lote que a atendeu; (c) apagar requisições canceladas.
- **Consequências.** (+) Uma só fonte de movimentação de estoque (lote/pedido, D7); histórico completo do que foi pedido, por quem e quando. O botão "Solicitar" do card "Repor primeiro" no dashboard já abre a requisição preenchida. (−) Não há ligação automática requisição ↔ lote: marcar como atendida é manual.

### D18. Saída do aluno em vez de exclusão

- **Contexto.** Quando um aluno deixava a escola, a secretaria usava "Excluir". O `aluno` tem soft delete com `@SQLRestriction("deletado = false")`, e essa restrição vale também quando o Hibernate carrega o aluno a partir de um `pedido`: as entregas passavam a apontar para um aluno "invisível" e a listagem de entregas e os relatórios quebravam. **Essa foi a causa raiz do bug.**
- **Decisão.** Novas colunas `aluno.ativo bool NOT NULL DEFAULT true` e `aluno.data_saida timestamp NULL` (V3), com `PATCH /api/aluno/{id}/saida` e `/reativar`. Aluno inativo continua listado (filtro `?ativo=`), no histórico e nos relatórios, mas `PedidoService` recusa novas entregas para ele (editar uma entrega antiga dele continua permitido). `DELETE /api/aluno/{id}` passou a recusar aluno com entregas. A V3 também recuperou os alunos já excluídos que tinham entregas, transformando-os em "desligados". Como defesa extra, `Pedido.aluno` usa `@NotFound(IGNORE)` e a resposta mostra "Aluno removido" se o cadastro não carregar.
- **Alternativas consideradas.** (a) Remover o `@SQLRestriction` e filtrar excluídos à mão; (b) exclusão física com `ON DELETE CASCADE`; (c) manter só o soft delete.
- **Consequências.** (+) "Saiu da escola" e "cadastrei errado" viram operações diferentes, e o histórico de uniformes recebidos nunca se perde. (−) Duas flags no aluno (`deletado` e `ativo`) com significados diferentes, que precisam ser explicadas a quem mantém o código.

### D19. VIEW `vw_estoque` como fonte única da regra de situação

- **Contexto.** A regra "esgotado / baixo / ok" estava repetida no relatório, na tela de estoque e no dashboard, e versões antigas usavam um limite fixo de 5 peças.
- **Decisão.** `V4__view_estoque.sql` cria a VIEW `vw_estoque` (uniforme + tipo, com a coluna `situacao`: `ESGOTADO` se `quantidade <= 0`, `BAIXO` se `quantidade <= estoque_baixo`, senão `OK`). O painel (`GET /api/uniforme/painel`) e o relatório de estoque leem a view pela projeção `EstoqueView`. `Uniforme.estaComEstoqueBaixo()`, `SituacaoEstoque.calcular` e `lib/estoque.ts` (front) seguem exatamente a mesma regra, e há testes para o caso de fronteira (`quantidade = limite` é BAIXO).
- **Alternativas consideradas.** (a) Coluna calculada/trigger em `uniforme`; (b) VIEW materializada; (c) calcular só no Java.
- **Consequências.** (+) A regra fica documentada no próprio schema e é consultável por SQL; não há dado duplicado (a view é sempre atual). (−) A regra ainda existe em três lugares (view, Java, TypeScript), mantidos alinhados por testes.

### D20. Linhas repetidas são somadas, não recusadas

- **Contexto.** Nos formulários de entrada e de entrega o usuário pode lançar a mesma peça em duas linhas.
- **Decisão.** Backend soma as quantidades antes de gravar (`ItemLoteService.consolidarItens` com a chave `ChaveItemLote`, D9; `PedidoUniformeService.consolidarItens` por `uniformeId`), com `Math.addExact` para recusar estouro. O frontend mostra o aviso "Mesma peça em N linhas: serão somadas" e um resumo somado por peça antes de salvar.
- **Alternativas consideradas.** Recusar com erro de validação; criar uma linha por item repetido.
- **Consequências.** (+) Nenhum lançamento é perdido e cada lote/entrega tem no máximo uma linha por variação. (−) Sem `UNIQUE` no banco: a regra vive só na aplicação.

### D21. Locks de estoque em ordem determinística

- **Contexto.** Duas entregas (ou lotes) simultâneas com as mesmas peças em ordens diferentes podiam travar linhas de `uniforme` em ordem cruzada e entrar em *deadlock*.
- **Decisão.** Antes de mexer no saldo, `UniformeService.travarEstoque` / `travarEstoquePorCombinacao` fazem `SELECT ... FOR UPDATE` em **todas** as linhas envolvidas (itens novos e antigos, na edição), sem repetição e **ordenadas** por id/combinação. Só depois as quantidades são alteradas.
- **Consequências.** (+) A segunda transação só espera a primeira terminar, nunca há espera circular. (−) Trava algumas linhas um pouco antes do estritamente necessário; irrelevante para o volume de uma escola.

### D22. Migrations versionadas daqui em diante

- **Contexto.** Ver D3: até o commit `1063a46` a V1 era editada no lugar.
- **Decisão.** A V1 fica congelada; cada mudança de schema ou correção de dados é uma nova versão `V<n>__descricao.sql` (V2 a V5 nesta rodada). `docs/ddl.sql` é só a concatenação delas, para leitura.
- **Consequências.** (+) Bancos existentes evoluem sem recriação; o histórico de schema fica auditável. (−) O DDL completo não está mais num arquivo único da aplicação (por isso o `ddl.sql` de documentação).

---

## Parte 2 — Decisões de arquitetura e segurança

### A1. Autenticação JWT stateless

- **Contexto.** Frontend SPA separado do backend, consumindo uma API REST.
- **Decisão.** Spring Security com `SessionCreationPolicy.STATELESS` e CSRF desabilitado. O login (`POST /api/auth/login`) devolve um JWT HMAC-256 (`com.auth0:java-jwt`), com emissor `6M-Uniformes`, `subject` = e-mail e validade de 36.000 s (10 h), assinado com o segredo `JWT_SECRET`. O `SecurityFilter` valida o token do cabeçalho `Authorization: Bearer` a cada requisição. Senhas são armazenadas com `BCryptPasswordEncoder`. Rotas públicas: login, registro de usuário, troca de senha (que exige a senha atual) e Swagger. O frontend guarda a sessão no `localStorage` e envia o cabeçalho em `lib/api-client.ts`.
- **Alternativas consideradas.** Sessão HTTP com cookie (stateful); OAuth2/Keycloak.
- **Consequências.** (+) Backend sem estado de sessão, simples de escalar e de rodar em Docker; integração direta com a SPA. (−) Não há revogação de token antes de expirar nem refresh token; token no `localStorage` é exposto a XSS. `PUT /api/usuario/trocar-senha` continua sem token (serve a quem não consegue entrar), mas agora exige `senhaAtual`: e-mail inexistente e senha errada recebem a mesma resposta (`400 Senha atual incorreta`), sem revelar quais e-mails existem. `PUT /api/usuario/atualizar` aceita reenviar o próprio e-mail.

### A2. Relatórios em PDF ou CSV gerados no servidor

- **Contexto.** A escola precisa de relatórios para prestação de contas (estoque, entradas, saídas, entregas por turma e transações).
- **Decisão.** `RelatorioService` monta os dados e `shared/pdf/PdfTableBuilder` gera um PDF tabular com a biblioteca **OpenPDF** (`com.github.librepdf:openpdf:2.0.3`). O `RelatorioController` responde em `/api/relatorio/{estoque|entrada|saida|entregas-turma|transacoes}`, com filtro de período resolvido por `RelatorioPeriodoResolver` e `?formato=pdf|csv` (padrão `pdf`). O CSV (`shared/csv/CsvTableBuilder`) é UTF-8 com BOM e separador `;`, para abrir direto no Excel. Período sem dados **não é erro**: o arquivo sai com o cabeçalho e "Nenhum registro encontrado" (antes a API devolvia 404 e o usuário não sabia se era falha).
- **Alternativas consideradas.** iText (licença AGPL/comercial), JasperReports (mais pesado, exige templates), geração no navegador.
- **Consequências.** (+) Licença LGPL/MPL compatível com o projeto, layout padronizado e igual para todos os usuários. (−) Layout definido em código Java; mudanças visuais exigem novo deploy do backend.

### A3. Organização do backend por domínio e DTOs `record`

- **Decisão.** Pacotes por domínio (`domain/aluno`, `domain/lote`, ...), cada um com `Entity`, `Repository`, `Service`, `Controller` e `dto/` com `record`s validados por Bean Validation. Erros de negócio usam `BadRequestException`/`NotFoundException` tratadas em `RestExceptionHandler` (que também converte `DataIntegrityViolationException`). API documentada com springdoc/Swagger.
- **Consequências.** (+) Fácil localizar tudo de uma funcionalidade; entidades JPA nunca são expostas diretamente na API.

### A4. 401 para sessão inválida, 403 para falta de permissão

- **Contexto.** Sem configuração, o Spring Security responde `403` tanto para "sem token/token expirado" quanto para "sem permissão". O frontend não conseguia distinguir sessão expirada de erro de permissão e o usuário ficava preso numa tela com erros.
- **Decisão.** `security/RestAuthenticationEntryPoint` responde **`401`** com JSON (`{"message":"Sessão expirada ou inválida. Entre novamente."}`) quando o token está ausente, é inválido ou expirou; `403` fica reservado para usuário autenticado sem permissão. O `api-client` do frontend trata o 401 limpando a sessão e redirecionando ao login.
- **Consequências.** (+) Semântica HTTP correta, contrato igual ao anotado no Swagger, e sessão expirada vira um redirecionamento limpo. (−) Nenhum relevante.

---

## Parte 3 — Decisões de stack

| Camada | Tecnologia (versão no repositório) | Justificativa |
|---|---|---|
| Backend | **Java 25** + **Spring Boot 4.1** (Web, Data JPA, Security, Validation), Lombok | Ecossistema maduro e conhecido da equipe; Spring Data JPA elimina boilerplate de acesso a dados, Spring Security fornece autenticação/autorização prontas, e Bean Validation centraliza regras de entrada. Tipagem forte reduz erros em regras de estoque. |
| Banco de dados | **PostgreSQL 18** (imagem `postgres:18-alpine`) | Banco relacional livre e robusto, adequado a dados com integridade referencial forte (estoque, histórico). Recursos usados de forma específica: `ENUM` nativo, `gen_random_uuid()`, índices funcionais (`lower(...)`), `CHECK` e `SELECT ... FOR UPDATE`. |
| Migrations | **Flyway** | Schema versionado junto com o código e aplicado automaticamente (ver D3). |
| Relatórios | **OpenPDF 2.0.3** (PDF) + CSV próprio | Ver A2. |
| Documentação da API | **springdoc-openapi** (Swagger UI) | Contrato da API navegável, usado pelo time de frontend. |
| Testes backend | JUnit 5, Mockito, **Testcontainers** (PostgreSQL) | Testes unitários dos serviços e testes de integração (`FluxoCompletoIntegrationTest`, `SegurancaIntegrationTest`, `AlunoSaidaIntegrationTest`) com um PostgreSQL real em container, executando as mesmas migrations Flyway. 235 testes no total. |
| Frontend | **React 19** + **TypeScript** + **Vite 8** | SPA com build e recarregamento rápidos; TypeScript tipa os contratos da API (`lib/types`). |
| Estilo e componentes | **Tailwind CSS 4** + **shadcn/ui** (Radix/Base UI, lucide-react) | Componentes acessíveis e copiados para o projeto (customizáveis, sem dependência de um kit fechado); utilitários do Tailwind dão consistência visual. O commit `b0e299e` padronizou as telas em WCAG/shadcn. |
| Estado de servidor | **TanStack Query 5** | Cache, revalidação e invalidação após mutações (hooks `use-alunos`, `use-pedidos`, `use-lotes`...), evitando gerenciar manualmente loading/erro/refetch. Configurado em `App.tsx` com `staleTime` de 1 min e sem retry automático. |
| Formulários | React Hook Form + Zod | Validação declarativa no cliente espelhando as regras dos DTOs. |
| Testes frontend | Vitest + Testing Library | Integrados ao Vite; 86 testes. |
| Infraestrutura | **Docker** + Docker Compose | `backend/dockerfile` em dois estágios (build com JDK 25, execução com JRE 25 em usuário sem privilégios) e `backend/docker-compose.yml` com os serviços `db` (com healthcheck) e `backend`. Garante que todos rodem o mesmo PostgreSQL e o mesmo backend sem instalar Java/Postgres localmente. O frontend roda com o servidor do Vite (`host 0.0.0.0` para acesso na rede local) e não possui Dockerfile próprio. |
