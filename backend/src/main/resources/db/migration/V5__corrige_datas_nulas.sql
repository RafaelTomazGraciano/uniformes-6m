-- Entregas e lotes gravados sem data sumiam dos relatórios, que filtram por período.
-- As tabelas não têm data de criação, então o melhor palpite disponível é o momento da migração.
UPDATE pedido SET data_efetivada = now() WHERE data_efetivada IS NULL;
UPDATE lote SET data_entrega = now() WHERE data_entrega IS NULL;

-- Daqui em diante a data é obrigatória: o backend preenche com o momento do registro quando ela não vem no corpo.
ALTER TABLE pedido ALTER COLUMN data_efetivada SET DEFAULT now();
ALTER TABLE pedido ALTER COLUMN data_efetivada SET NOT NULL;
ALTER TABLE lote ALTER COLUMN data_entrega SET DEFAULT now();
ALTER TABLE lote ALTER COLUMN data_entrega SET NOT NULL;
