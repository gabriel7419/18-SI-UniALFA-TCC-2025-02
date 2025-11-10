-- Script de migração para permitir produtos não perecíveis
-- Execute este script no MySQL antes de iniciar a aplicação

-- 1. Adicionar a coluna nao_perecivel se não existir
ALTER TABLE produto 
ADD COLUMN IF NOT EXISTS nao_perecivel BOOLEAN DEFAULT FALSE NOT NULL;

-- 2. Alterar a coluna data_de_vencimento para permitir NULL
ALTER TABLE produto 
MODIFY COLUMN data_de_vencimento DATE NULL;

-- 3. Atualizar produtos existentes sem data de vencimento (se houver)
-- Marcar como não perecíveis produtos que não tem data
UPDATE produto 
SET nao_perecivel = TRUE 
WHERE data_de_vencimento IS NULL;
