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

/**
 * Cliente para comunicación con el Property Service.
 * Maneja todas las peticiones relacionadas con propiedades.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.property-service.url}")
    private String propertyServiceUrl;

    /**
     * Obtiene información completa de una propiedad por su ID.
     * @param propertyId ID de la propiedad
     * @return DTO con información de la propiedad, o null si no existe
     */
    public PropiedadDTO getPropertyById(Long propertyId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(propertyServiceUrl + "/api/propiedades/" + propertyId)
                    .retrieve()
                    .bodyToMono(PropiedadDTO.class)
                    .timeout(Duration.ofSeconds(5))
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

    /**
     * Verifica si existe una propiedad con el ID dado.
     * @param propertyId ID de la propiedad
     * @return true si existe, false en caso contrario
     */
    public boolean existsProperty(Long propertyId) {
        try {
            PropiedadDTO property = getPropertyById(propertyId);
            return property != null && property.getId() != null;
        } catch (Exception e) {
            log.error("Error al verificar existencia de la propiedad {}: {}", propertyId, e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el ID del propietario de una propiedad.
     * @param propertyId ID de la propiedad
     * @return ID del propietario, o null si no se pudo obtener
     */
    public Long getPropertyOwnerId(Long propertyId) {
        try {
            PropiedadDTO property = getPropertyById(propertyId);
            return property != null ? property.getPropietarioId() : null;
        } catch (Exception e) {
            log.error("Error al obtener propietario de la propiedad {}: {}", propertyId, e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si una propiedad pertenece a un usuario específico.
     * @param propertyId ID de la propiedad
     * @param userId ID del usuario
     * @return true si la propiedad pertenece al usuario, false en caso contrario
     */
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