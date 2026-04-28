package com.rentify.applicationService.client;

import com.rentify.applicationService.dto.PropiedadDTO;
import com.rentify.applicationService.exception.MicroserviceException;
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

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.property-service.url}")
    private String propertyServiceUrl;

    public PropiedadDTO getPropertyById(Long propertyId) {
        try {
            log.info("🔍 PropertyServiceClient: Intentando obtener propiedad {} desde URL: {}/api/propiedades/{}",
                    propertyId, propertyServiceUrl, propertyId);

            PropiedadDTO propiedad = webClientBuilder.build()
                    .get()
                    .uri(propertyServiceUrl + "/api/propiedades/" + propertyId)
                    .retrieve()
                    .bodyToMono(PropiedadDTO.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(error -> {
                        log.error(" Error al obtener propiedad {}: {} - {}",
                                propertyId, error.getClass().getSimpleName(), error.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (propiedad != null) {
                log.info("Propiedad {} encontrada: ID={}, Título={}, PrecioMensual={}",
                        propertyId, propiedad.getId(), propiedad.getTitulo(), propiedad.getPrecioMensual());
            } else {
                log.warn("⚠ PropertyServiceClient retornó NULL para propiedad {}", propertyId);
            }

            return propiedad;
        } catch (Exception e) {
            log.error(" Error crítico al comunicarse con Property Service: {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
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

    /**
     * Verifica si una propiedad está disponible
     * Por ahora asumimos que si existe, está disponible
     * Esto puede mejorarse cuando Property Service implemente disponibilidad
     */
    public boolean isPropertyAvailable(Long propertyId) {
        try {
            PropiedadDTO property = getPropertyById(propertyId);
            // Por ahora, si la propiedad existe, la consideramos disponible
            // En el futuro, Property Service podría devolver un campo "disponible"
            return property != null;
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad de la propiedad {}: {}", propertyId, e.getMessage());
            return false;
        }
    }
}