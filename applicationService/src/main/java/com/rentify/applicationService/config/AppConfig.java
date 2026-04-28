package com.rentify.applicationService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configuración STRICT para evitar mapeos ambiguos
        modelMapper.getConfiguration()
                .setMatchingStrategy(org.modelmapper.convention.MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        // Mapeo explícito: SolicitudArriendoDTO -> SolicitudArriendo
        modelMapper.createTypeMap(com.rentify.applicationService.dto.SolicitudArriendoDTO.class,
                        com.rentify.applicationService.model.SolicitudArriendo.class)
                .addMappings(mapper -> {
                    mapper.map(com.rentify.applicationService.dto.SolicitudArriendoDTO::getUsuarioId,
                            com.rentify.applicationService.model.SolicitudArriendo::setUsuarioId);
                    mapper.map(com.rentify.applicationService.dto.SolicitudArriendoDTO::getPropiedadId,
                            com.rentify.applicationService.model.SolicitudArriendo::setPropiedadId);
                    // Ignorar campos que se setean después
                    mapper.skip(com.rentify.applicationService.model.SolicitudArriendo::setEstado);
                    mapper.skip(com.rentify.applicationService.model.SolicitudArriendo::setFechaSolicitud);
                });

        // Mapeo explícito: RegistroArriendoDTO -> RegistroArriendo
        modelMapper.createTypeMap(com.rentify.applicationService.dto.RegistroArriendoDTO.class,
                        com.rentify.applicationService.model.RegistroArriendo.class)
                .addMappings(mapper -> {
                    mapper.map(com.rentify.applicationService.dto.RegistroArriendoDTO::getSolicitudId,
                            com.rentify.applicationService.model.RegistroArriendo::setSolicitudId);
                    // Ignorar campo que se setea después
                    mapper.skip(com.rentify.applicationService.model.RegistroArriendo::setActivo);
                });

        return modelMapper;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        System.out.println("========================================");
        System.out.println("🔥 SWAGGER CONFIGURADO");
        System.out.println("📍 URL: http://localhost:8084/swagger-ui/index.html");
        System.out.println("========================================");

        return new OpenAPI()
                .info(new Info()
                        .title("Rentify - Application Service API")
                        .version("1.0")
                        .description("API para gestión de solicitudes y registros de arriendo")
                        .contact(new Contact()
                                .name("Rentify Team")
                                .email("support@rentify.com")));
    }
}