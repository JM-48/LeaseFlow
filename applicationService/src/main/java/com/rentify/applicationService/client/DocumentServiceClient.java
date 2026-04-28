package com.rentify.applicationService.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Cliente para comunicación con el Document Service
 * Maneja la verificación de documentos de usuarios
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.document-service.url}")
    private String documentServiceUrl;

    /**
     * Verifica si un usuario tiene todos los documentos requeridos aprobados
     *
     * @param userId ID del usuario a verificar
     * @return true si tiene todos los documentos aprobados, false en caso contrario
     */
    public boolean hasApprovedDocuments(Long userId) {
        try {
            log.debug("Verificando documentos aprobados para usuario {}", userId);

            Boolean hasDocuments = webClientBuilder.build()
                    .get()
                    .uri(documentServiceUrl + "/api/documentos/usuario/" + userId + "/verificar-aprobados")
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(error -> {
                        log.error("Error al verificar documentos del usuario {}: {}", userId, error.getMessage());
                        return Mono.just(false);
                    })
                    .block();

            boolean result = Boolean.TRUE.equals(hasDocuments);
            log.debug("Usuario {} tiene documentos aprobados: {}", userId, Boolean.valueOf(result));  // CORREGIDO

            return result;
        } catch (Exception e) {
            log.error("Error crítico al comunicarse con Document Service para usuario {}: {}",
                    userId, e.getMessage());
            // En producción podrías querer lanzar una excepción aquí
            // Por ahora retornamos false para ser más permisivos durante desarrollo
            return false;
        }
    }

    /**
     * Obtiene la cantidad de documentos aprobados de un usuario
     *
     * @param userId ID del usuario
     * @return cantidad de documentos aprobados
     */
    public int countApprovedDocuments(Long userId) {
        try {
            log.debug("Contando documentos aprobados para usuario {}", userId);

            Integer count = webClientBuilder.build()
                    .get()
                    .uri(documentServiceUrl + "/api/documentos/usuario/" + userId + "/contar-aprobados")
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(error -> {
                        log.error("Error al contar documentos del usuario {}: {}", userId, error.getMessage());
                        return Mono.just(0);
                    })
                    .block();

            int result = count != null ? count : 0;
            log.debug("Usuario {} tiene {} documentos aprobados", userId, Integer.valueOf(result));  // CORREGIDO

            return result;
        } catch (Exception e) {
            log.error("Error crítico al comunicarse con Document Service: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica si el Document Service está disponible
     *
     * @return true si el servicio responde, false en caso contrario
     */
    public boolean isServiceAvailable() {
        try {
            webClientBuilder.build()
                    .get()
                    .uri(documentServiceUrl + "/actuator/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Document Service no está disponible: {}", e.getMessage());
            return false;
        }
    }
}