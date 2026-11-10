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
