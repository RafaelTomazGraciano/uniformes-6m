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
