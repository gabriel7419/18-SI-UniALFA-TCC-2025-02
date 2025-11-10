# Migração: Produtos Não Perecíveis

## 📋 Descrição
Esta migração adiciona suporte para produtos não perecíveis/sem data de vencimento no sistema AlberguePro.

## 🔧 Alterações no Banco de Dados

### 1. Nova coluna: `nao_perecivel`
- Tipo: BOOLEAN
- Padrão: FALSE
- Permite NULL: NÃO

### 2. Alteração da coluna: `data_de_vencimento`
- Mudança: Agora permite valores NULL
- Antes: NOT NULL
- Depois: NULL

## 🚀 Como Aplicar a Migração

### Opção 1: Executar o Script SQL Manualmente (RECOMENDADO)

1. **Conecte-se ao MySQL:**
   ```bash
   mysql -u root -p
   ```

2. **Selecione o banco de dados:**
   ```sql
   USE alberguepro;
   ```

3. **Execute o script de migração:**
   ```bash
   source Documentation/migration_produto_nao_perecivel.sql
   ```
   
   OU copie e cole o conteúdo do arquivo `migration_produto_nao_perecivel.sql` diretamente no MySQL.

### Opção 2: Deixar o Hibernate Atualizar (Pode não funcionar completamente)

1. **Parar a aplicação** se estiver rodando
2. **Iniciar a aplicação** - O Hibernate tentará adicionar a nova coluna `nao_perecivel`
3. **Se der erro** de "Column cannot be null", execute o script SQL manualmente (Opção 1)

## ✅ Verificação

Após aplicar a migração, verifique se as alterações foram aplicadas:

```sql
-- Verificar a estrutura da tabela
DESCRIBE produto;

-- Você deve ver:
-- - nao_perecivel: tinyint(1), Default: 0
-- - data_de_vencimento: date, NULL: YES
```

## 📝 Funcionalidades Adicionadas

1. ✅ Checkbox para marcar produto como não perecível
2. ✅ Campo de data de vencimento opcional quando marcado como não perecível
3. ✅ Validação condicional: data obrigatória apenas para produtos perecíveis
4. ✅ Exibição "Não Perecível" nas listagens e relatórios
5. ✅ Interface dinâmica que esconde/mostra campo de data automaticamente

## ⚠️ Importante

- **Backup**: Faça backup do banco de dados antes de executar a migração
- **Produtos existentes**: Produtos sem data de vencimento serão marcados automaticamente como não perecíveis
- **Reversão**: Para reverter, você pode executar:
  ```sql
  ALTER TABLE produto MODIFY COLUMN data_de_vencimento DATE NOT NULL;
  ALTER TABLE produto DROP COLUMN nao_perecivel;
  ```

## 🐛 Resolução de Problemas

### Erro: "Column 'data_de_vencimento' cannot be null"
**Solução**: Execute o script SQL manualmente (Opção 1)

### Erro: "Column 'nao_perecivel' already exists"
**Solução**: A coluna já foi criada. Execute apenas a parte 2 do script:
```sql
ALTER TABLE produto MODIFY COLUMN data_de_vencimento DATE NULL;
```

### Erro ao iniciar a aplicação após a migração
**Solução**: 
1. Verifique se as duas colunas foram criadas/alteradas corretamente
2. Reinicie a aplicação
3. Limpe o cache do Maven se necessário: `mvn clean install`
