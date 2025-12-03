# FIFO System - Sistema de Controle Logístico

Sistema de gestão logística de alta performance focado na metodologia **FIFO (First-In, First-Out)**. A aplicação gerencia o fluxo de entrada e saída de pacotes, rastreamento via QR Code, monitoramento em tempo real e auditoria completa de operações.

Este projeto foi modernizado para uma arquitetura **Enterprise** utilizando **Java 21** e **Spring Boot 3**, garantindo escalabilidade, segurança e manutenibilidade.

---

## 🚀 Stack Tecnológica

O projeto adota as práticas de mercado mais recentes para desenvolvimento de software corporativo.

### Backend (Java Ecosystem)
* **Core:** Java 21, Spring Boot 3.4
* **Persistência:** Spring Data JPA (Hibernate), PostgreSQL
* **Database Migration:** Flyway (Versionamento de Schema e Dados)
* **Segurança:** Spring Security 6, JWT (Stateless Authentication), BCrypt
* **Documentação:** SpringDoc OpenAPI (Swagger UI)
* **Testes:** JUnit 5, Mockito
* **Tempo Real:** Spring WebSocket

### Frontend
* **Framework:** React 18 (Vite)
* **Estilização:** CSS Modules (Design Responsivo e Dark Mode)
* **Integração:** Axios (Interceptor para JWT)
* **Utils:** QRCode.react, jsPDF

### DevOps & Infra
* **Containerização:** Docker, Docker Compose (Multi-stage build)
* **CI/CD:** GitHub Actions (Pipeline de Build e Testes)

---

## 🏛️ Arquitetura e Design

O backend segue princípios de **Clean Architecture**, priorizando a separação de responsabilidades para facilitar testes e evolução.

1.  **Domain Layer:** Entidades JPA (`User`, `ProductPackage`, `AuditLog`) isoladas, representando o núcleo do negócio.
2.  **Repository Layer:** Interfaces Spring Data para abstração do acesso a dados, utilizando **JPA Specifications** para consultas dinâmicas e filtros complexos.
3.  **Service Layer:** Contém todas as regras de negócio (ex: validação de duplicidade, cálculo de perfil de carga, lógica FIFO), desacoplada do framework web.
4.  **Controller Layer:** Camada REST que gerencia apenas a entrada/saída HTTP e validação de DTOs (`@Valid`).

### Destaques Técnicos
* **Auditoria Desacoplada:** Implementação de um serviço de *Auditing* que intercepta operações críticas (Entrada/Saída) e registra automaticamente o autor via Contexto de Segurança, sem poluir a lógica principal.
* **Database as Code:** Nenhuma tabela é criada manualmente. Todo o ciclo de vida do banco (DDL e DML de seed) é gerido via scripts SQL versionados pelo **Flyway**.
* **Soft Deletes:** Implementação de exclusão lógica para preservação de histórico operacional.
* **Monitoramento em Tempo Real:** WebSocket configurado para transmitir a lista de utilizadores online instantaneamente entre clientes conectados.

---

## 🛠️ Instalação e Execução

### Pré-requisitos
* Docker e Docker Compose instalados.
* **Ou:** JDK 21 e Maven configurados localmente.

### Opção A: Execução via Docker (Recomendado)
Para subir o ambiente completo (Banco + Backend + Frontend) em container:

```bash
docker-compose up --build
````

  * **Frontend:** [http://localhost:5173](https://www.google.com/search?q=http://localhost:5173)
  * **Backend API:** [http://localhost:8080](https://www.google.com/search?q=http://localhost:8080)

### Opção B: Execução Manual (Dev)

1.  **Banco de Dados:**
    Certifique-se de ter um PostgreSQL rodando e configure as variáveis no arquivo `backend-java/.env` ou variáveis de ambiente do sistema.

2.  **Backend:**

    ```bash
    cd backend-java
    ./mvnw spring-boot:run
    ```

3.  **Frontend:**

    ```bash
    cd frontend
    npm install
    npm run dev
    ```

-----

## 📚 Documentação da API

A API está totalmente documentada com **Swagger/OpenAPI**.
Após iniciar o backend, acesse:

👉 **[http://localhost:8080/swagger-ui/index.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui/index.html)**

Lá é possível testar todos os endpoints, incluindo autenticação e operações de pacotes.

-----

## 🧪 Testes Automatizados

O projeto possui cobertura de testes unitários para as regras de negócio críticas (Serviços de Usuário, Pacotes e QR Code).

Para executar a suite de testes:

```bash
cd backend-java
./mvnw test
```

-----

## 👤 Acesso Inicial

O sistema inicializa automaticamente (via Flyway Migration) com um utilizador administrador:

  * **Usuário:** `admin`
  * **Senha:** `admin`

> **Nota:** Recomenda-se alterar a senha no primeiro acesso através do painel de perfil.