package com.cts.mrfp.carrygo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Sets up Swagger / OpenAPI docs at /swagger-ui.html.
// Useful for testing the REST endpoints in a browser without writing a client.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI carryGoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CarryGo API")
                        .description("REST API for the CarryGo platform: users, deliveries, intercity courier, "
                                + "fare estimation, ratings, wallets, transactions, notifications, chat, and WebSocket streams.")
                        .version("v1")
                        .contact(new Contact().name("CarryGo Team"))
                        .license(new License().name("Internal")))
                .servers(List.of(new Server().url("/").description("Current host")));
    }
}
