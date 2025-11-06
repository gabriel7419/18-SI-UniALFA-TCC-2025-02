-- ============================================
-- MIGRAÇÃO RÁPIDA - COPIE E COLE NO MYSQL
-- ============================================

USE alberguepro;

-- Adicionar coluna nao_perecivel
ALTER TABLE produto 
ADD COLUMN nao_perecivel BOOLEAN DEFAULT FALSE NOT NULL;

-- Permitir NULL na coluna data_de_vencimento
ALTER TABLE produto 
MODIFY COLUMN data_de_vencimento DATE NULL;

-- Verificar se deu certo
DESCRIBE produto;

-- Pronto! Agora você pode reiniciar a aplicação.
