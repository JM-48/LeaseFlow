package com.rentify.contactService.config;

import com.rentify.contactService.dto.MensajeContactoDTO;
import com.rentify.contactService.model.MensajeContacto;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configuración para evitar ambigüedad con usuarioId vs usuario.id
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        // TypeMap específico: DTO -> Entity
        // Ignorar el campo 'usuario' al mapear de DTO a Entity
        modelMapper.createTypeMap(MensajeContactoDTO.class, MensajeContacto.class)
                .addMappings(mapper -> {
                    mapper.skip(MensajeContacto::setUsuarioId);
                })
                .setPostConverter(context -> {
                    MensajeContactoDTO source = context.getSource();
                    MensajeContacto destination = context.getDestination();

                    // Mapear usuarioId solo desde el campo directo, nunca desde usuario.id
                    if (source.getUsuarioId() != null) {
                        destination.setUsuarioId(source.getUsuarioId());
                    }

                    return destination;
                });

        return modelMapper;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rentify - Contact Service API")
                        .version("1.0")
                        .description("API para gestión de mensajes de contacto de usuarios")
                        .contact(new Contact()
                                .name("Rentify Team")
                                .email("support@rentify.com")));
    }
}