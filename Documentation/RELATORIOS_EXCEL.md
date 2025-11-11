# Implementação de Relatórios em Excel (.xlsx)

## Visão Geral
Foi implementada a funcionalidade de geração de relatórios em formato Excel (.xlsx) para todos os módulos do sistema AlberguePro, mantendo o design e estrutura semelhantes aos relatórios em PDF existentes.

## Dependência
O projeto já possui a dependência Apache POI necessária para manipulação de arquivos Excel:
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

## Relatórios Implementados

### 1. Relatório de Acolhidos
**Serviço:** `RelatorioAcolhidoService.gerarRelatorioExcel()`
**Endpoint:** `GET /cadastroAcolhido/relatorio/excel`
**Arquivo gerado:** `acolhidos.xlsx`

**Conteúdo:**
- Título: AlberguePro
- Subtítulo: Relatório de Acolhidos
- Data de emissão e usuário emissor
- Total de registros
- Tabela com colunas: ID, Nome, Data Nasc., Idade, Sexo, Naturalidade, RG, CPF, Data Ingresso

### 2. Relatório de Usuários
**Serviço:** `RelatorioUsuarioPatrimonioService.gerarRelatorioUsuarioExcel()`
**Endpoint:** `GET /relatorios/usuarios/excel`
**Arquivo gerado:** `usuarios.xlsx`

**Conteúdo:**
- Título: AlberguePro
- Subtítulo: Relatório de Usuários
- Data de emissão e usuário emissor
- Total de usuários e usuários ativos
- Tabela com colunas: ID, Username, Role, Ativo, Data Criação

### 3. Relatório de Patrimônio
**Serviço:** `RelatorioUsuarioPatrimonioService.gerarRelatorioPatrimonioExcel()`
**Endpoint:** `GET /relatorios/patrimonio/excel`
**Arquivo gerado:** `patrimonio.xlsx`

**Conteúdo:**
- Título: AlberguePro
- Subtítulo: Relatório de Patrimônio
- Data de emissão e usuário emissor
- Total de patrimônios
- Tabela com colunas: ID, Nome, Nº Patrimônio, Data Aquisição, Local Atual, Status
- Suporta filtros: nome, status, localAtual

### 4. Relatório de Estoque
**Serviço:** `RelatorioService.gerarRelatorioExcel()`
**Endpoint:** `GET /estoque/relatorio/excel`
**Arquivo gerado:** `estoque.xlsx`

**Conteúdo:**
- Título: AlberguePro
- Subtítulo: Relatório de Estoque
- Data de emissão e usuário emissor
- Total de itens e itens esgotados
- Tabela com colunas: ID, Tipo, Nome, Quantidade, Unidade, Data Vencimento
- Suporta filtros: nome, tipo, unidadeId

### 5. Relatório de Movimentações de Estoque
**Serviço:** `RelatorioService.gerarRelatorioMovimentacaoExcel()`
**Endpoint:** `GET /estoque/historico/relatorio/excel`
**Arquivo gerado:** `movimentacao_estoque.xlsx`

**Conteúdo:**
- Título: AlberguePro
- Subtítulo: Relatório de Movimentações de Estoque
- Data de emissão e usuário emissor
- Total de movimentações
- Tabela com colunas: ID, Produto, Tipo, Qtd. Movimentada, Qtd. Anterior, Qtd. Posterior, Data
- Suporta filtros: nomeProduto, tipo

## Design e Formatação

### Estilos Aplicados
Todos os relatórios seguem um padrão visual consistente:

1. **Título (AlberguePro)**
   - Fonte: Negrito, tamanho 20
   - Alinhamento: Centro
   - Mesclado em todas as colunas

2. **Subtítulo (Nome do Relatório)**
   - Fonte: Negrito, tamanho 16
   - Alinhamento: Centro
   - Mesclado em todas as colunas

3. **Informações do Relatório**
   - Data de emissão no formato: dd/MM/yyyy HH:mm:ss
   - Usuário que gerou o relatório
   - Estatísticas relevantes (totais, contadores, etc.)

4. **Cabeçalho da Tabela**
   - Fundo: Cinza claro (GREY_25_PERCENT)
   - Fonte: Negrito, tamanho 10
   - Bordas: Todas as células
   - Alinhamento: Centro

5. **Dados**
   - Bordas em todas as células
   - Alinhamento: Centro para IDs e números, Esquerda para texto
   - Datas formatadas: dd/MM/yyyy ou dd/MM/yyyy HH:mm

### Largura de Colunas
As larguras das colunas são ajustadas automaticamente para garantir boa legibilidade dos dados.

## Fuso Horário
Todos os relatórios utilizam o fuso horário América/São Paulo (GMT-3) para data e hora de emissão.

## Segurança
- Todos os endpoints mantêm a segurança do Spring Security já configurada no projeto
- O usuário emissor é capturado do contexto de autenticação

## Como Usar

### Exemplo de chamada via navegador:
```
http://localhost:8080/cadastroAcolhido/relatorio/excel
http://localhost:8080/relatorios/usuarios/excel
http://localhost:8080/relatorios/patrimonio/excel
http://localhost:8080/estoque/relatorio/excel
http://localhost:8080/estoque/historico/relatorio/excel
```

### Exemplo com filtros:
```
http://localhost:8080/relatorios/patrimonio/excel?status=Ativo&localAtual=Almoxarifado
http://localhost:8080/estoque/relatorio/excel?tipo=Alimento&nome=Arroz
```

## Integração com Interface
Os botões para download dos relatórios Excel podem ser adicionados nas views correspondentes, ao lado dos botões de relatório PDF existentes.

### Exemplo de botão HTML:
```html
<a href="/cadastroAcolhido/relatorio/excel" class="btn btn-success">
    <i class="fas fa-file-excel"></i> Baixar Excel
</a>
```

## Notas Técnicas
- Os arquivos Excel são gerados em memória usando `ByteArrayInputStream`
- Não há necessidade de templates JRXML como nos PDFs
- O formato XLSX é compatível com Microsoft Excel 2007+, LibreOffice Calc e Google Sheets
- Os relatórios mantêm a mesma lógica de filtros e queries dos relatórios PDF
