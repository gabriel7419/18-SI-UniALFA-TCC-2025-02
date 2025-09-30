# AlberguePro

Projeto AlberguePro SpringBoot

## Sumário

O AlberguePro é um sistema web desenvolvido para a gestão de albergues para pessoas em situação de vulnerabilidade. O objetivo principal da aplicação é fornecer uma plataforma centralizada para o cadastro e acompanhamento dos acolhidos, bem como a gestão dos recursos do albergue.

### Funcionalidades

*   **Gestão de Usuários:** O sistema permite o cadastro de dois tipos de usuários: Administradores e usuários comuns, cada um com diferentes níveis de acesso.
*   **Gestão de Acolhidos:** Permite o cadastro de informações completas sobre os acolhidos, incluindo dados pessoais, informações familiares e datas de entrada e saída.
*   **Controle de Estoque:** O sistema disponibiliza um módulo para o gerenciamento do estoque de alimentos, produtos de higiene e limpeza.
*   **Controle de Patrimônio:** Permite o cadastro e controle dos bens patrimoniais da instituição.
*   **Gestão de Leitos e Vagas:** O sistema oferece um módulo para a gestão de leitos e vagas, permitindo a alocação dos acolhidos em leitos disponíveis.

### Tecnologia

A aplicação foi desenvolvida utilizando as seguintes tecnologias:

*   **Backend:** Java com o framework Spring Boot.
*   **Frontend:** Thymeleaf.
*   **Banco de Dados:** MySQL.

## Como executar com Docker

1. **Construa a aplicação:**

```bash
./mvnw clean install
```

2. **Inicie os containers:**

```bash
docker-compose up -d
```

3. **Acesse a aplicação:**

A aplicação estará disponível em [http://localhost:8081](http://localhost:8081).

**Observação:** O banco de dados MySQL estará acessível na porta `3307`.

## Como parar a aplicação

```bash
docker-compose down
```