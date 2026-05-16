package com.rentify.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración de beans de la aplicación User Service
 * Incluye ModelMapper para DTOs, WebClient para comunicación entre microservicios
 * y OpenAPI para documentación Swagger
 */
@Configuration
public class AppConfig {

    /**
     * Bean de ModelMapper para convertir entre DTOs y Entidades
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Usar LOOSE para permitir mapeo flexible
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);

        return mapper;
    }

    /**
     * Bean de WebClient.Builder para comunicación con otros microservicios
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Configuración de OpenAPI/Swagger para documentación de la API
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rentify - User Service API")
                        .version("1.0")
                        .description("API para gestión de usuarios, roles y autenticación en Rentify. " +
                                "Maneja registro, login, actualización de datos y asignación de roles.")
                        .contact(new Contact()
                                .name("Rentify Team")
                                .email("support@rentify.com")));
    }
}