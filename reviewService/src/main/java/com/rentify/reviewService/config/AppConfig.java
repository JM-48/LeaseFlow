package com.rentify.reviewService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración principal de la aplicación ReviewService.
 * Define beans necesarios para el funcionamiento del microservicio.
 */
@Configuration
public class AppConfig {

    /**
     * Bean de ModelMapper para conversión entre DTOs y Entidades.
     * @return instancia configurada de ModelMapper
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Bean de WebClient.Builder para comunicación con otros microservicios.
     * @return builder configurado de WebClient
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Configuración de OpenAPI/Swagger para documentación de la API.
     * @return configuración personalizada de OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rentify - Review Service API")
                        .version("1.0")
                        .description("API para gestión de reseñas y valoraciones de propiedades y usuarios en la plataforma Rentify")
                        .contact(new Contact()
                                .name("Rentify Team")
                                .email("support@rentify.com")));
    }
}