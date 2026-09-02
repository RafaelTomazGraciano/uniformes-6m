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
  'NOTURNO'
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
                                 "tipo" varchar(255) NOT NULL
);

CREATE TABLE "uniforme" (
                            "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                            "tipo_id" uuid NOT NULL,
                            "tamanho" tamanho NOT NULL,
                            "quantidade" int NOT NULL DEFAULT 0,
                            "sexo" sexo NOT NULL,
                            "deletado" bool NOT NULL DEFAULT false
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
                         "deletado" bool NOT NULL DEFAULT false
);

CREATE TABLE "pedido_uniforme" (
                                   "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                                   "uniforme_id" uuid NOT NULL,
                                   "pedido_id" uuid NOT NULL,
                                   "quantidade" int NOT NULL
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
                             "sexo" sexo NOT NULL
);

CREATE TABLE "lote" (
                        "id" uuid PRIMARY KEY DEFAULT (gen_random_uuid()),
                        "nota_fiscal_id" uuid NOT NULL,
                        "fornecedor" varchar(255) NOT NULL,
                        "data_entrega" timestamp
);

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
