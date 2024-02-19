# Rinha de Backend 2024

Minha submissão para o desafio da [rinha de backend 2024](https://github.com/zanfranceschi/rinha-de-backend-2024-q1)

## Java + Spring Boot + GraalVM

<p>Stack escolhida foi Spring Boot, compilado para executável nativo com GraalVM, e banco de dados PostgreSQL</p>
<p>Projeto está separado em dois módulos Maven</p>

- domain: 
  - contém todas as classes de domínio da aplicação, como entidades (não as do JPA 👀), exceptions, _ports_ e _use cases_
  - pom.xml declara apenas dependências simples, como JUnit, Mockito e uma lib de utilitários
  - esse módulo é totalmente independente de framework e usa conceito de Inversão de Dependências para se tornar extensível
- spring-app:
  - app Spring Boot com dependências de `spring-web` e `spring-data-jdbc`
  - implementa as _ports_ declaradas no domínio (Ports and Adapters) para comunicação com banco de dados
  - roda as APIs http que invocam os _use cases_ do domínio

<p>Minha decisão por arquitetar dessa forma foi para trabalhar e estudar conceitos de DDD e Arquitetura Hexagonal. 
Com o domínio bem isolado, posso ter minha aplicação rodando com Spring, Quarkus e até mesmo serverless com AWS Lambda ou algum outro provedor de cloud</p>

<p>Exemplo dessa flexibilidade de implantação da app pode ser vista nos módulos [quarkus-app](https://github.com/andre237/rinha-de-backend-2024-q1-java/tree/multi-framework/quarkus-app) e [serverless-app](https://github.com/andre237/rinha-de-backend-2024-q1-java/tree/multi-framework/serverless-app), 
onde o mesmo domínio está implementado em dois frameworks diferentes, sem qualquer dependência ou acoplamento</p>

