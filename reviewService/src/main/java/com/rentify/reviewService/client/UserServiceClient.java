package com.rentify.reviewService.client;

import com.rentify.reviewService.dto.external.UsuarioDTO;
import com.rentify.reviewService.exception.MicroserviceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Cliente para comunicación con el User Service.
 * Maneja todas las peticiones relacionadas con usuarios.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.user-service.url}")
    private String userServiceUrl;

    /**
     * Obtiene información completa de un usuario por su ID.
     * @param userId ID del usuario
     * @return DTO con información del usuario, o null si no existe
     */
    public UsuarioDTO getUserById(Long userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/usuarios/" + userId)
                    .retrieve()
                    .bodyToMono(UsuarioDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(error -> {
                        log.error("Error al obtener usuario {}: {}", userId, error.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error crítico al comunicarse con User Service: {}", e.getMessage());
            throw new MicroserviceException("No se pudo verificar el usuario. Intente nuevamente.");
        }
    }

    /**
     * Verifica si existe un usuario con el ID dado.
     * @param userId ID del usuario
     * @return true si existe, false en caso contrario
     */
    public boolean existsUser(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            return user != null && user.getId() != null;
        } catch (Exception e) {
            log.error("Error al verificar existencia del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un usuario tiene un rol específico.
     * @param userId ID del usuario
     * @param rol rol a verificar
     * @return true si el usuario tiene ese rol, false en caso contrario
     */
    public boolean hasRole(Long userId, String rol) {
        try {
            UsuarioDTO user = getUserById(userId);
            return user != null && rol.equals(user.getRol());
        } catch (Exception e) {
            log.error("Error al verificar rol del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el rol de un usuario.
     * @param userId ID del usuario
     * @return rol del usuario, o null si no se pudo obtener
     */
    public String getUserRole(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            return user != null ? user.getRol() : null;
        } catch (Exception e) {
            log.error("Error al obtener rol del usuario {}: {}", userId, e.getMessage());
            return null;
        }
    }
}