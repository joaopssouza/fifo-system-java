package com.joaopssouza.fifosystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIFO System API")
                        .version("1.0")
                        .description("Documentação da API de Gestão Logística (Spring Boot 3 + Java 21)")
                        .contact(new Contact()
                                .name("João Paulo S. Souza")
                                .email("joaop0737@gmail.com") // Coloque seu email real aqui
                                .url("https://github.com/joaopssouza")) // Seu GitHub
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                // Configuração para o botão "Authorize" (JWT)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}