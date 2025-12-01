# FIFO System (Spring Boot Edition) 🚀

Portfólio técnico desenvolvido para demonstrar proficiência em engenharia de software backend com **Java 21** e **Spring Boot 3**. Este projeto é uma migração e modernização de um sistema de gestão logística, aplicando padrões de projeto Enterprise.

## 🛠️ Tecnologias Utilizadas

* **Java 21:** Uso de *Records*, *Pattern Matching* e *Switch Expressions*.
* **Spring Boot 3.4:** Framework principal.
* **Spring Data JPA:** Camada de persistência e abstração de banco de dados.
* **Flyway:** Versionamento e migração de banco de dados (*Database as Code*).
* **Spring Security + JWT:** Autenticação *Stateless* e segura.
* **PostgreSQL:** Banco de dados relacional.
* **JUnit 5 & Mockito:** Testes unitários para garantia de qualidade.
* **Docker:** Containerização da aplicação (Multi-stage build).
* **Swagger (OpenAPI):** Documentação interativa da API.

## 🏛️ Arquitetura e Design

O projeto segue a **Clean Architecture** simplificada, focada na separação de responsabilidades:

1.  **Controller Layer:** Apenas recebe requisições HTTP e valida DTOs.
2.  **Service Layer:** Contém toda a regra de negócio (ex: validação de duplicidade, cálculo de perfil).
3.  **Repository Layer:** Interfaces JPA para acesso a dados.
4.  **Security Layer:** Filtros e Providers desacoplados para gestão de JWT.

### Destaques
* **Auditoria Automática:** Um serviço de `AuditService` utiliza o `SecurityContext` para registrar automaticamente quem realizou cada ação (Entrada/Saída/Movimentação), sem poluir a lógica de negócio.
* **Tratamento de Erros:** Exceções de negócio são capturadas e transformadas em respostas HTTP adequadas.

## 🚀 Como Rodar

### Pré-requisitos
* Java 21
* Docker (opcional, para banco de dados)

### Passos
1.  Configure as variáveis de ambiente no arquivo `.env`:
    ```ini
    DB_PASSWORD=sua_senha_supabase
    JWT_SECRET=seu_segredo_super_seguro
    ```
2.  Execute a aplicação:
    ```bash
    ./mvnw spring-boot:run
    ```
3.  Acesse a documentação Swagger:
    👉 `http://localhost:8080/swagger-ui/index.html`

## ✅ Testes
Para executar a suite de testes unitários:
```bash
./mvnw test