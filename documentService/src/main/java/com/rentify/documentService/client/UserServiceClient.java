package com.rentify.documentService.client;

import com.rentify.documentService.dto.external.UsuarioDTO;
import com.rentify.documentService.exception.MicroserviceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    private static final String USUARIO_ID_HEADER = "X-Usuario-Id";
    private static final String ROL_ID_HEADER = "X-Rol-Id";
    private static final String INTERNAL_ROL_ID = "1";

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.user-service.url}")
    private String userServiceUrl;

    @Value("${app.security.client-key}")
    private String appClientKey;

    public UsuarioDTO getUserById(Long userId) {
        try {
            log.debug("Consultando usuario con ID: {} en URL: {}", userId, userServiceUrl);

            UsuarioDTO usuario = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/usuarios/" + userId)
                    .header(APP_CLIENT_HEADER, appClientKey)
                    .header(USUARIO_ID_HEADER, String.valueOf(userId))
                    .header(ROL_ID_HEADER, INTERNAL_ROL_ID)
                    .retrieve()
                    .bodyToMono(UsuarioDTO.class)
                    .timeout(Duration.ofSeconds(15))
                    .onErrorResume(WebClientResponseException.NotFound.class, error -> {
                        log.warn("Usuario {} no encontrado en User Service (404)", userId);
                        return Mono.empty();
                    })
                    .onErrorResume(WebClientResponseException.class, error -> {
                        log.error("Error HTTP al obtener usuario {}: {} - {}",
                                userId, error.getStatusCode(), error.getMessage());
                        return Mono.empty();
                    })
                    .onErrorResume(Exception.class, error -> {
                        log.error("Error de conexión al obtener usuario {}: {}",
                                userId, error.getClass().getSimpleName() + " - " + error.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (usuario != null) {
                log.debug("Usuario {} encontrado: {} {}", userId, usuario.getPnombre(), usuario.getPapellido());
            } else {
                log.warn("Usuario {} no encontrado o error al consultar", userId);
            }

            return usuario;

        } catch (Exception e) {
            log.error("Error crítico al comunicarse con User Service para usuario {}: {}",
                    userId, e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new MicroserviceException("No se pudo verificar el usuario. Intente nuevamente.");
        }
    }

    public boolean existsUser(Long userId) {
        try {
            log.debug("Verificando existencia del usuario con ID: {}", userId);
            UsuarioDTO user = getUserById(userId);
            boolean exists = user != null && user.getId() != null;
            log.debug("Usuario {} existe: {}", userId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error al verificar existencia del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }

    public boolean userHasRole(Long userId, String rol) {
        try {
            log.debug("Verificando si usuario {} tiene rol: {}", userId, rol);
            UsuarioDTO user = getUserById(userId);
            boolean hasRole = user != null && user.getRol() != null && rol.equals(user.getRol().getNombre());
            log.debug("Usuario {} tiene rol {}: {}", userId, rol, hasRole);
            return hasRole;
        } catch (Exception e) {
            log.error("Error al verificar rol del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }
}