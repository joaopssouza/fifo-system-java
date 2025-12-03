package com.joaopssouza.fifosystem; // ou o seu package correto

import io.github.cdimascio.dotenv.Dotenv; // <-- Importar isto
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FifoSystemSpringBootApplication {

    public static void main(String[] args) {
        // 1. Carregar o .env
        // O parametro ignoreIfMissing=true evita erro se o arquivo não existir (útil para produção onde se usa vars reais)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        // 2. Injetar as variáveis no Sistema para o Spring conseguir ler (ex: ${DB_PASSWORD})
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        // 3. Iniciar o Spring Boot
        SpringApplication.run(FifoSystemSpringBootApplication.class, args);
    }

}