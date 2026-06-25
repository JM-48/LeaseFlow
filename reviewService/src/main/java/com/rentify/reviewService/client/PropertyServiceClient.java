package com.rentify.reviewService.client;

import com.rentify.reviewService.dto.external.PropiedadDTO;
import com.rentify.reviewService.exception.MicroserviceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceClient {

    private static final String APP_CLIENT_HEADER = "X-App-Client";

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.property-service.url}")
    private String propertyServiceUrl;

    @Value("${app.security.client-key}")
    private String appClientKey;

    public PropiedadDTO getPropertyById(Long propertyId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(propertyServiceUrl + "/api/propiedades/" + propertyId)
                    .header(APP_CLIENT_HEADER, appClientKey)
                    .retrieve()
                    .bodyToMono(PropiedadDTO.class)
                    .timeout(Duration.ofSeconds(15))
                    .onErrorResume(error -> {
                        log.error("Error al obtener propiedad {}: {}", propertyId, error.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error crítico al comunicarse con Property Service: {}", e.getMessage());
            throw new MicroserviceException("No se pudo verificar la propiedad. Intente nuevamente.");
        }
    }

    public boolean existsProperty(Long propertyId) {
        try {
            PropiedadDTO property = getPropertyById(propertyId);
            return property != null && property.getId() != null;
        } catch (Exception e) {
            log.error("Error al verificar existencia de la propiedad {}: {}", propertyId, e.getMessage());
            return false;
        }
    }

    public Long getPropertyOwnerId(Long propertyId) {
        try {
            PropiedadDTO property = getPropertyById(propertyId);
            return property != null ? property.getPropietarioId() : null;
        } catch (Exception e) {
            log.error("Error al obtener propietario de la propiedad {}: {}", propertyId, e.getMessage());
            return null;
        }
    }

    public boolean isPropertyOwner(Long propertyId, Long userId) {
        try {
            Long ownerId = getPropertyOwnerId(propertyId);
            return ownerId != null && ownerId.equals(userId);
        } catch (Exception e) {
            log.error("Error al verificar propiedad del usuario: {}", e.getMessage());
            return false;
        }
    }
}