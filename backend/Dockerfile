# backend/Dockerfile

# Estágio 1: Build (Compilação do Java)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila o JAR pulando testes para agilizar o build do container
RUN mvn clean package -DskipTests

# Estágio 2: Runtime (Imagem leve para rodar)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia o JAR gerado no estágio anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]