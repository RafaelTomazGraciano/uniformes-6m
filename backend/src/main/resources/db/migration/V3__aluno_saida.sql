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
