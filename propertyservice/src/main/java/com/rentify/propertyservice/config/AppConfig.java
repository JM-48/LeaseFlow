package com.rentify.propertyservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración principal de la aplicación PropertyService.
 * Define beans necesarios para el funcionamiento del microservicio.
 */
@Configuration
public class AppConfig {

    /**
     * Bean de ModelMapper para conversión entre DTOs y Entidades.
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Bean de WebClient.Builder para comunicación con otros microservicios.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Configuración de ObjectMapper para Jackson.
     * Registra el módulo JavaTimeModule para soportar LocalDate, LocalDateTime, etc.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())  // ✅ AGREGADO
                .build();
    }

    /**
     * Configuración de OpenAPI/Swagger para documentación de la API.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rentify - Property Service API")
                        .version("1.0")
                        .description("API para gestión de propiedades, fotos, categorías, comunas, regiones y tipos en Rentify")
                        .contact(new Contact()
                                .name("Rentify Team")
                                .email("support@rentify.com")));
    }
}