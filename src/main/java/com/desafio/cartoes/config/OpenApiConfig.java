package com.desafio.cartoes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados exibidos no Swagger UI (/swagger-ui.html) e no contrato OpenAPI (/v3/api-docs).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cartoesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Oferta de Cartões de Crédito")
                        .description("Recebe os dados de um cliente e retorna os cartões de crédito "
                                + "para os quais ele é elegível, de acordo com renda, idade e UF.")
                        .version("v1")
                        .contact(new Contact().name("Eloisa Laurentino")));
    }
}
