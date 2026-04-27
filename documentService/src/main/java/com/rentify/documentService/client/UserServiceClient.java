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

/**
 * Cliente WebClient para comunicación con User Service.
 * Proporciona métodos para consultar información de usuarios.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.user-service.url}")
    private String userServiceUrl;

    /**
     * Obtiene un usuario por su ID desde User Service.
     *
     * @param userId ID del usuario a consultar
     * @return UsuarioDTO con la información del usuario, o null si no existe
     */
    public UsuarioDTO getUserById(Long userId) {
        try {
            log.debug("Consultando usuario con ID: {} en URL: {}", userId, userServiceUrl);

            UsuarioDTO usuario = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/usuarios/" + userId)
                    .retrieve()
                    .bodyToMono(UsuarioDTO.class)
                    .timeout(Duration.ofSeconds(10)) // Aumentado a 10 segundos
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

    /**
     * Verifica si un usuario existe en el sistema.
     *
     * @param userId ID del usuario a verificar
     * @return true si el usuario existe, false en caso contrario
     */
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

    /**
     * Verifica si un usuario tiene un rol específico.
     *
     * @param userId ID del usuario
     * @param rol Rol a verificar
     * @return true si el usuario tiene el rol especificado
     */
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