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

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    // Headers de negocio requeridos por DocumentoController (protege todos sus endpoints)
    private static final String USUARIO_ID_HEADER = "X-Usuario-Id";
    private static final String ROL_ID_HEADER = "X-Rol-Id";
    // Rol ADMIN interno para llamadas server-to-server
    private static final String INTERNAL_ROL_ID = "1";

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.document-service.url}")
    private String documentServiceUrl;

    @Value("${app.security.client-key}")
    private String appClientKey;

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
                    .header(APP_CLIENT_HEADER, appClientKey)
                    .header(USUARIO_ID_HEADER, String.valueOf(userId))
                    .header(ROL_ID_HEADER, INTERNAL_ROL_ID)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(15))
                    .onErrorResume(error -> {
                        log.error("Error al verificar documentos del usuario {}: {}", userId, error.getMessage());
                        return Mono.just(false);
                    })
                    .block();

            boolean result = Boolean.TRUE.equals(hasDocuments);
            log.debug("Usuario {} tiene documentos aprobados: {}", userId, Boolean.valueOf(result));

            return result;
        } catch (Exception e) {
            log.error("Error crítico al comunicarse con Document Service para usuario {}: {}",
                    userId, e.getMessage());
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
                    .header(APP_CLIENT_HEADER, appClientKey)
                    .header(USUARIO_ID_HEADER, String.valueOf(userId))
                    .header(ROL_ID_HEADER, INTERNAL_ROL_ID)
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .timeout(Duration.ofSeconds(15))
                    .onErrorResume(error -> {
                        log.error("Error al contar documentos del usuario {}: {}", userId, error.getMessage());
                        return Mono.just(0);
                    })
                    .block();

            int result = count != null ? count : 0;
            log.debug("Usuario {} tiene {} documentos aprobados", userId, Integer.valueOf(result));

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
                    .header(APP_CLIENT_HEADER, appClientKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Document Service no está disponible: {}", e.getMessage());
            return false;
        }
    }
}