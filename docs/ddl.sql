-- =============================================================================
-- DDL completo do banco de dados do sistema 6M Uniformes (PostgreSQL)
--
-- Fonte: as migrations Flyway em backend/src/main/resources/db/migration/,
-- executadas automaticamente, em ordem, na subida do backend:
--   V1__create_initial_schema.sql  enums, 11 tabelas, índices e FKs
--   V2__create_requisicao.sql      enum status_requisicao + tabela requisicao
--   V3__aluno_saida.sql            aluno.ativo / aluno.data_saida
--   V4__view_estoque.sql           VIEW vw_estoque (situação ESGOTADO/BAIXO/OK)
--   V5__corrige_datas_nulas.sql    datas de pedido e lote NOT NULL DEFAULT now()
--
-- Este arquivo é apenas a concatenação delas, para leitura. A fonte da verdade
-- são as migrations; em caso de divergência, vale o arquivo da migration.
-- Para recriar o banco do zero, execute este script inteiro em ordem.
-- =============================================================================


-- =============================================================================
-- V1__create_initial_schema.sql
-- =============================================================================

CREATE TYPE "sexo" AS ENUM (
  'MASCULINO',
  'FEMININO'
);

CREATE TYPE "tamanho" AS ENUM (
  'PP',
  'P',
  'M',
  'G',
  'GG'
);

CREATE TYPE "tipo_escola" AS ENUM (
  'PUBLICA',
  'TECNICA',
  'PARCEIRA',
  'CIVICO_MILITAR',
  'MILITAR'
);

CREATE TYPE "turno" AS ENUM (
  'DIURNO',
  'VESPERTINO',
  'NOTURNO',
  'INTEGRAL'
);

CREATE TYPE "ensino" AS ENUM (
  'FUNDAMENTAL',
  'MEDIO',
  'TECNICO'
);

CREATE TABLE "escola" (
                          "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                          "nome" varchar(255) NOT NULL,
                          "tipo" tipo_escola NOT NULL,
                          "endereco" varchar(255) NOT NULL
);

CREATE TABLE "usuario" (
                           "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                           "nome" varchar(255) NOT NULL,
                           "email" varchar(255) UNIQUE NOT NULL,
                           "senha" varchar(255) NOT NULL,
                           "deletado" bool NOT NULL DEFAULT false
);

CREATE TABLE "tipo_uniforme" (
                                 "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                                 "tipo" varchar(255) NOT NULL,
                                 "estoque_baixo" int NOT NULL
);

CREATE TABLE "uniforme" (
                            "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                            "tipo_id" uuid NOT NULL,
                            "tamanho" tamanho NOT NULL,
                            "quantidade" int NOT NULL DEFAULT 0,
                            "sexo" sexo NOT NULL,
                            CONSTRAINT "ck_uniforme_quantidade" CHECK (quantidade >= 0)
);

CREATE TABLE "turma" (
                         "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                         "nome" varchar(255) NOT NULL,
                         "turno" turno NOT NULL,
                         "ensino" ensino NOT NULL
);

CREATE TABLE "aluno" (
                         "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                         "turma_id" uuid NOT NULL,
                         "nome" varchar(255) NOT NULL,
                         "nome_responsavel" varchar(255),
                         "telefone_responsavel" varchar(11),
                         "deletado" bool NOT NULL DEFAULT false
);

CREATE TABLE "pedido_uniforme" (
                                   "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                                   "uniforme_id" uuid NOT NULL,
                                   "pedido_id" uuid NOT NULL,
                                   "quantidade" int NOT NULL,
                                   CONSTRAINT "ck_pedido_uniforme_quantidade" CHECK (quantidade > 0)
);

CREATE TABLE "pedido" (
                          "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                          "aluno_id" uuid NOT NULL,
                          "usuario_id" uuid NOT NULL,
                          "data_efetivada" timestamp
);

CREATE TABLE "nota_fiscal" (
                               "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                               "chave_acesso" varchar(255) NOT NULL
);

CREATE TABLE "item_lote" (
                             "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                             "tipo_uniforme_id" uuid NOT NULL,
                             "lote_id" uuid NOT NULL,
                             "tamanho" tamanho NOT NULL,
                             "quantidade" int NOT NULL DEFAULT 0,
                             "sexo" sexo NOT NULL,
                             CONSTRAINT "ck_item_lote_quantidade" CHECK (quantidade > 0)
);

CREATE TABLE "lote" (
                        "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                        "nota_fiscal_id" uuid NOT NULL,
                        "fornecedor" varchar(255) NOT NULL,
                        "data_entrega" timestamp
);

CREATE UNIQUE INDEX "uq_tipo_uniforme_tipo" ON "tipo_uniforme" ((lower(tipo)));

CREATE UNIQUE INDEX "uq_uniforme_tipo_tamanho_sexo" ON "uniforme" ("tipo_id", "tamanho", "sexo");

CREATE UNIQUE INDEX "uq_turma_nome" ON "turma" ((lower(nome)));

CREATE INDEX "idx_nome" ON "aluno" ("nome");

ALTER TABLE "uniforme" ADD FOREIGN KEY ("tipo_id") REFERENCES "tipo_uniforme" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "aluno" ADD FOREIGN KEY ("turma_id") REFERENCES "turma" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "pedido_uniforme" ADD FOREIGN KEY ("uniforme_id") REFERENCES "uniforme" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "pedido_uniforme" ADD FOREIGN KEY ("pedido_id") REFERENCES "pedido" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "pedido" ADD FOREIGN KEY ("aluno_id") REFERENCES "aluno" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "pedido" ADD FOREIGN KEY ("usuario_id") REFERENCES "usuario" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "item_lote" ADD FOREIGN KEY ("tipo_uniforme_id") REFERENCES "tipo_uniforme" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "item_lote" ADD FOREIGN KEY ("lote_id") REFERENCES "lote" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "lote" ADD FOREIGN KEY ("nota_fiscal_id") REFERENCES "nota_fiscal" ("id") DEFERRABLE INITIALLY IMMEDIATE;


-- =============================================================================
-- V2__create_requisicao.sql
-- =============================================================================

CREATE TYPE "status_requisicao" AS ENUM (
  'PENDENTE',
  'ATENDIDA',
  'CANCELADA'
);

CREATE TABLE "requisicao" (
                              "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                              "tipo_uniforme_id" uuid NOT NULL,
                              "tamanho" tamanho NOT NULL,
                              "sexo" sexo NOT NULL,
                              "quantidade" int NOT NULL,
                              "observacao" text,
                              "status" status_requisicao NOT NULL DEFAULT 'PENDENTE',
                              "usuario_id" uuid NOT NULL,
                              "data_criacao" timestamp NOT NULL DEFAULT (now()),
                              "data_conclusao" timestamp,
                              CONSTRAINT "ck_requisicao_quantidade" CHECK (quantidade > 0)
);

CREATE INDEX "idx_requisicao_status" ON "requisicao" ("status");

ALTER TABLE "requisicao" ADD FOREIGN KEY ("tipo_uniforme_id") REFERENCES "tipo_uniforme" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "requisicao" ADD FOREIGN KEY ("usuario_id") REFERENCES "usuario" ("id") DEFERRABLE INITIALLY IMMEDIATE;


-- =============================================================================
-- V3__aluno_saida.sql
-- =============================================================================

-- Saída (desligamento) do aluno: o cadastro fica inativo, mas continua existindo
-- para que o histórico de entregas, que referencia o aluno, permaneça íntegro.
ALTER TABLE "aluno" ADD COLUMN "ativo" bool NOT NULL DEFAULT true;
ALTER TABLE "aluno" ADD COLUMN "data_saida" timestamp NULL;

-- Antes desta migração, "Excluir" fazia soft delete mesmo em alunos com entregas,
-- e essas entregas passavam a apontar para um aluno invisível (a listagem quebrava).
-- Esses alunos voltam como desligados, preservando o histórico.
UPDATE "aluno" a
SET "deletado"   = false,
    "ativo"      = false,
    "data_saida" = now()
WHERE a."deletado" = true
  AND EXISTS (SELECT 1 FROM "pedido" p WHERE p."aluno_id" = a."id");

CREATE INDEX "idx_aluno_ativo" ON "aluno" ("ativo");


-- =============================================================================
-- V4__view_estoque.sql
-- =============================================================================

-- Visão do estoque usada pelo painel (GET /api/uniforme/painel) e pelo relatório de estoque.
-- A regra de situação é a mesma de Uniforme.estaComEstoqueBaixo() e de lib/estoque.ts no front:
-- 0 peças = ESGOTADO; quantidade <= limite do tipo = BAIXO (o próprio limite já conta como baixo); acima disso = OK.
CREATE VIEW vw_estoque AS
SELECT u.id            AS uniforme_id,
       t.id            AS tipo_uniforme_id,
       t.tipo          AS tipo,
       u.tamanho       AS tamanho,
       u.sexo          AS sexo,
       u.quantidade    AS quantidade,
       t.estoque_baixo AS estoque_baixo,
       CASE
           WHEN u.quantidade <= 0 THEN 'ESGOTADO'
           WHEN u.quantidade <= t.estoque_baixo THEN 'BAIXO'
           ELSE 'OK'
       END             AS situacao
FROM uniforme u
         JOIN tipo_uniforme t ON t.id = u.tipo_id;


-- =============================================================================
-- V5__corrige_datas_nulas.sql
-- =============================================================================

-- Entregas e lotes gravados sem data sumiam dos relatórios, que filtram por período.
-- As tabelas não têm data de criação, então o melhor palpite disponível é o momento da migração.
UPDATE pedido SET data_efetivada = now() WHERE data_efetivada IS NULL;
UPDATE lote SET data_entrega = now() WHERE data_entrega IS NULL;

-- Daqui em diante a data é obrigatória: o backend preenche com o momento do registro quando ela não vem no corpo.
ALTER TABLE pedido ALTER COLUMN data_efetivada SET DEFAULT now();
ALTER TABLE pedido ALTER COLUMN data_efetivada SET NOT NULL;
ALTER TABLE lote ALTER COLUMN data_entrega SET DEFAULT now();
ALTER TABLE lote ALTER COLUMN data_entrega SET NOT NULL;

