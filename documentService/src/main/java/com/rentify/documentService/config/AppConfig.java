package com.rentify.documentService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración principal de la aplicación Document Service.
 * Define los beans necesarios para el funcionamiento del microservicio.
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
     * Builder de WebClient para comunicación con otros microservicios.
     * @return WebClient.Builder configurado
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Configuración de OpenAPI/Swagger para documentación automática.
     * @return configuración personalizada de OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rentify - Document Service API")
                        .version("1.0")
                        .description("API para gestión de documentos de usuarios en Rentify. " +
                                "Permite subir, consultar y validar documentos requeridos para " +
                                "el proceso de arriendo (liquidaciones, certificados, etc.)")
                        .contact(new Contact()
                                .name("Rentify Team")
                                .email("support@rentify.com")));
    }
}