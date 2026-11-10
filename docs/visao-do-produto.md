# Documento de visão do produto — Uniformes 6M

| | |
|---|---|
| **Produto** | Uniformes 6M — sistema de gestão de uniformes escolares |
| **Responsável** | Product Owner da equipe |
| **Versão** | 1.0 (entrega final da disciplina) |

---

## 1. Visão em uma frase

**Para** a secretaria e o almoxarifado de escolas que recebem e distribuem uniformes, **o Uniformes 6M é** um sistema web de controle de estoque **que** registra cada peça que entra (lote do fornecedor) e cada peça que sai (entrega ao aluno), avisa quando um tamanho está acabando, registra a requisição de reposição e gera em segundos os relatórios em PDF ou CSV exigidos na prestação de contas. **Diferente de** planilhas e cadernos de controle, ele mantém o saldo sempre correto, sabe quem recebeu o quê e não deixa entregar uma peça que não existe no estoque.

## 2. O problema

Escolas recebem uniformes em lotes, muitas vezes de mais de um fornecedor e com nota fiscal, e precisam distribuí-los aos alunos ao longo do ano. Hoje esse controle costuma ser feito em planilhas, cadernos ou fichas de papel. Isso causa:

| Problema | Consequência |
| -------- | ------------ |
| O saldo de cada tamanho é calculado à mão | Divergência entre o que a planilha diz e o que há na prateleira; descobre-se a falta só quando o aluno chega. |
| Não há registro confiável de quem recebeu cada peça | Alunos recebem em dobro ou ficam sem receber; difícil responder a pais e à direção. |
| Entradas não ficam vinculadas à nota fiscal | Na prestação de contas, é trabalhoso provar o que foi recebido de cada fornecedor. |
| Relatórios são montados manualmente no fim do período | Horas de trabalho repetitivo, sujeito a erro, toda vez que a gestão ou um órgão pede números. |
| Ninguém é avisado de que um tamanho está acabando | A reposição é pedida tarde, depois que o estoque zerou. |
| O pedido de reposição fica em mensagem ou papel | Ninguém sabe o que já foi solicitado, por quem, nem se chegou. |
| Aluno que sai da escola é apagado da planilha | Some junto o histórico do que ele recebeu. |

## 3. Por que construir

- **Confiabilidade do saldo.** O estoque é atualizado pelo próprio sistema a cada entrada e entrega, numa única transação. Não há conta manual a errar.
- **Rastreabilidade.** Cada entrega registra aluno, peças, data e o usuário que fez o lançamento. Cada entrada registra fornecedor, data e chave da NF-e.
- **Prestação de contas sem retrabalho.** Os cinco relatórios (estoque, entradas, saídas, entregas por turma e transações) saem em PDF, prontos para imprimir ou anexar, ou em CSV para abrir no Excel.
- **Reposição antecipada.** Cada tipo de uniforme tem seu limite de "estoque baixo"; o sistema mostra o que repor primeiro antes que acabe e, com um clique em "Solicitar", registra a requisição ao fornecedor.
- **Histórico que não se perde.** Aluno que sai da escola é desligado, não apagado: as entregas que recebeu continuam nos relatórios.

## 4. Para quem — personas

### Persona 1 — Rosa, almoxarife / auxiliar de secretaria (usuária principal)

| | |
|---|---|
| **Quem é** | Funcionária da secretaria responsável por receber as caixas do fornecedor e entregar os uniformes aos alunos. |
| **Contexto** | Atende filas de alunos no início do ano letivo; usa o computador da secretaria; não é da área de tecnologia. |
| **Objetivos** | Lançar rapidamente a entrada de um lote com dezenas de itens; entregar ao aluno o tamanho certo sem precisar conferir prateleira por prateleira; achar um aluno pelo nome ou pela turma. |
| **Dores** | Planilha que "não fecha"; descobrir no balcão que o tamanho acabou; não lembrar se um aluno já recebeu. |
| **O que o sistema oferece** | Lançamento de lote em lista ou em grade (tamanho × sexo); nova entrega com catálogo que bloqueia tamanhos sem estoque e limita a quantidade ao saldo; busca de aluno por nome ou turma (sem se preocupar com acentos); histórico de entregas por aluno; registro de saída do aluno que deixou a escola. |

### Persona 2 — Carlos, coordenador / gestão escolar

| | |
|---|---|
| **Quem é** | Coordenador administrativo ou diretor que responde pelo uso dos uniformes perante a rede de ensino e a comunidade. |
| **Contexto** | Acessa o sistema esporadicamente, para acompanhar números e pedir reposição. |
| **Objetivos** | Saber de relance quantas peças há, quantas foram entregues no mês e o que está acabando; gerar relatórios oficiais por período e por turma. |
| **Dores** | Pedir números à secretaria e esperar dias; relatórios inconsistentes entre si. |
| **O que o sistema oferece** | Visão geral (dashboard) com totais, entregas do mês, tamanhos esgotados, matriz de estoque por tamanho e lista "Repor primeiro" com botão "Solicitar"; acompanhamento das requisições pendentes; relatórios em PDF ou CSV filtrados por mês, intervalo de meses ou ano. |

## 5. Escopo — funcionalidades principais

| # | Funcionalidade | O que entrega | Persona |
| - | -------------- | ------------- | ------- |
| 1 | **Autenticação** | Cadastro de usuário, login com JWT (sessão de 10 h), troca de senha com confirmação da senha atual e edição dos dados da conta. Sessão expirada leva de volta ao login com aviso. | Ambas |
| 2 | **Cadastro de turmas** | Turmas com turno e nível de ensino, com contagem de alunos. Não permite excluir turma com alunos. | Rosa |
| 3 | **Cadastro de alunos** | Aluno vinculado à turma, com responsável e telefone. Busca por nome ou turma, filtro por turma e situação (ativos / saíram). | Rosa |
| 3a | **Saída de aluno** | "Registrar saída" desliga o aluno que deixou a escola: ele continua no histórico e nos relatórios, mas não recebe novas entregas; pode ser reativado. Excluir fica só para cadastro feito por engano (recusado se o aluno já recebeu uniforme). | Rosa |
| 4 | **Tipos de uniforme** | Catálogo de peças (camiseta, bermuda, agasalho…) com o limite de estoque baixo de cada uma. | Rosa / Carlos |
| 5 | **Entrada de lote** | Registro da entrega do fornecedor com chave da NF-e (única), fornecedor, data e itens por tipo, tamanho e sexo. O estoque sobe automaticamente. Lote pode ser corrigido sem deixar o estoque negativo. | Rosa |
| 6 | **Entrega ao aluno** | Registro de quais peças cada aluno recebeu. O estoque desce automaticamente; o sistema recusa quantidade maior que o saldo. Registra quem fez o lançamento. | Rosa |
| 7 | **Estoque** | Saldo de cada combinação tipo + tamanho + sexo, em tabela ou grade, com selos "Esgotado" e "Baixo" (limite do tipo; a quantidade igual ao limite já é "Baixo") e atalho "Repor" que abre a nova entrada já preenchida. | Ambas |
| 8 | **Visão geral (dashboard)** | Peças em estoque, entregas do mês, alunos cadastrados, tamanhos esgotados, matriz de estoque, últimas entregas e "Repor primeiro" com botão "Solicitar". | Carlos |
| 8a | **Requisição de novos uniformes** | Registro do que precisa ser pedido ao fornecedor (tipo, tamanho, sexo, quantidade, observação), com status pendente → atendida ou cancelada, abas "Em andamento" e "Atendidas e canceladas", e quem/quando solicitou. Não mexe no estoque: as peças entram pelo lote. | Ambas |
| 9 | **Relatórios em PDF ou CSV** | Estoque atual, entradas, saídas, entregas por turma (todas ou uma) e transações (entradas + saídas + saldo), por mês, intervalo ou ano, em PDF ou CSV (Excel). Período sem dados gera o arquivo com "Nenhum registro encontrado", nunca erro. | Carlos |
| 10 | **API documentada** | API REST com Swagger e documentação de endpoints, permitindo integrações futuras. | Equipe técnica |

### Regras de negócio que o produto garante

- O estoque nunca fica negativo: entregas acima do saldo e correções de lote que estornariam peças já entregues são recusadas.
- Entrada e saída são atômicas: ou todos os itens são lançados, ou nenhum.
- Uma mesma NF-e não pode ser lançada duas vezes.
- Alerta de estoque baixo é por tipo de uniforme (ex.: avisar com 8 bermudas, mas com 2 calças de agasalho).
- Entregas e lotes não são apagados, só corrigidos: o histórico é a base da prestação de contas.
- Aluno que saiu da escola não recebe novas entregas, mas o que ele já recebeu continua registrado.
- Toda entrega e todo lote têm data: se não for informada, vale o momento do registro.
- A mesma peça lançada em duas linhas é somada, nunca perdida.
- Requisição não altera o estoque; só uma requisição pendente pode ser atendida ou cancelada.

## 6. Fora de escopo (nesta versão)

| Item | Motivo / alternativa |
| ---- | -------------------- |
| Perfis e permissões diferentes (ex.: só leitura para a gestão) | Todos os usuários autenticados têm o mesmo acesso. Candidato para a próxima versão. |
| Várias escolas no mesmo sistema (multi-tenant) | A entidade Escola existe no modelo, mas o sistema opera como uma única unidade. |
| Compras e pedidos de compra formais ao fornecedor | O sistema registra a **requisição** interna (o que precisa ser pedido) e o que chegou, mas não emite pedido de compra nem liga automaticamente a requisição ao lote. |
| Leitura automática da NF-e (XML/SEFAZ) | A chave de acesso é digitada; não há validação junto à Receita. |
| Devolução e troca de uniforme pelo aluno | Pode ser feita corrigindo a entrega, mas não há fluxo próprio. |
| Assinatura ou termo de recebimento do responsável | Fora do escopo; o registro é feito pelo usuário da secretaria. |
| Aplicativo móvel nativo e modo offline | A interface web funciona no navegador da rede local. |
| Notificações por email/WhatsApp de estoque baixo | O alerta é visual, dentro do sistema. |
| Exclusão de lotes e entregas | Proposital, para preservar a trilha de auditoria. |

## 7. Critérios de sucesso

### Critérios de aceite do produto (verificáveis na demonstração)

- [ ] Ao lançar um lote, o saldo de cada tamanho aumenta exatamente na quantidade lançada.
- [ ] Ao registrar uma entrega, o saldo diminui e a entrega aparece no histórico do aluno e na visão geral.
- [ ] O sistema impede entregar mais peças do que há em estoque e mostra a mensagem do motivo.
- [ ] Um tamanho abaixo do limite do seu tipo aparece como "Baixo" (ou "Esgotado") no estoque e em "Repor primeiro".
- [ ] É possível encontrar um aluno pelo nome ou pela turma.
- [ ] Os cinco relatórios geram PDF e CSV para o período escolhido (mesmo sem dados), e o de entregas por turma mostra subtotal por turma.
- [ ] Pelo botão "Solicitar" do "Repor primeiro" é possível criar uma requisição já preenchida, que aparece em "Em andamento" e pode ser marcada como atendida ou cancelada.
- [ ] Um aluno com saída registrada aparece como "Saiu em <data>", mantém o histórico e não pode receber nova entrega.
- [ ] Todas as telas, exceto login, cadastro e troca de senha, exigem login; sessão expirada volta ao login com aviso.

### Indicadores de sucesso no uso real

| Indicador | Meta |
| --------- | ---- |
| Divergência entre saldo do sistema e contagem física no inventário | 0 peças |
| Tempo para registrar a entrega de um aluno | menos de 1 minuto |
| Tempo para gerar um relatório de prestação de contas | menos de 1 minuto (antes: horas) |
| Tamanhos que chegam a zero sem aviso prévio | nenhum (todos passam por "Baixo" antes) |
| Entregas sem registro de aluno e responsável pelo lançamento | 0% |

### Critérios de qualidade técnica

- Testes automatizados no backend (235 testes: unitários com Mockito e integração com Testcontainers + Postgres real) e no frontend (86 testes com Vitest + Testing Library).
- API documentada (Swagger e `docs/endpoints.md`).
- Interface acessível (rótulos, estados de carregando/erro/vazio, mensagens de erro em português junto ao campo).

## 8. Restrições e premissas

- Executado localmente ou na rede da escola: backend em Docker (Java 25, Spring Boot, PostgreSQL) e frontend React servido pelo Vite.
- Premissa: há um computador na secretaria com navegador moderno e acesso à máquina que roda o backend.
- Premissa: os uniformes são identificados por tipo, tamanho (PP a GG) e sexo (masculino/feminino).

## 9. Referências

- [README do projeto](../README.md)
- [README do frontend](../frontend/README.md) — telas e padrões de interface
- [BACKEND.md](../backend/BACKEND.md) — arquitetura, banco e testes
- [Documentação dos endpoints](./endpoints.md)
- [Modelo de dados (ERD)](./ERD.md) e [decisões de modelagem](./decisoes-de-modelagem.md)
- [Roteiro da apresentação final](./roteiro-apresentacao.md)
