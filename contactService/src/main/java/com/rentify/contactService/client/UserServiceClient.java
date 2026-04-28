package com.rentify.contactService.client;

import com.rentify.contactService.dto.external.UsuarioDTO;
import com.rentify.contactService.exception.MicroserviceException;
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
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.user-service.url}")
    private String userServiceUrl;

    /**
     * Obtiene un usuario por su ID desde el User Service
     * @param userId ID del usuario
     * @return UsuarioDTO con la información del usuario, o null si no existe
     */
    public UsuarioDTO getUserById(Long userId) {
        try {
            log.debug("Consultando usuario con ID: {} en {}", userId, userServiceUrl);

            return webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/usuarios/" + userId + "?includeDetails=true")
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
     * Verifica si un usuario existe
     * @param userId ID del usuario
     * @return true si el usuario existe, false en caso contrario
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
     * Verifica si un usuario tiene el rol de ADMIN
     * @param userId ID del usuario
     * @return true si el usuario es admin, false en caso contrario
     */
    public boolean isAdmin(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            if (user == null) {
                return false;
            }

            // CAMBIO: Usar el método helper getRolNombre()
            String rolNombre = user.getRolNombre();
            return "ADMIN".equalsIgnoreCase(rolNombre);
        } catch (Exception e) {
            log.error("Error al verificar rol de administrador para usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el rol de un usuario
     * @param userId ID del usuario
     * @return Rol del usuario, o null si no se pudo obtener
     */
    public String getUserRole(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            // CAMBIO: Usar el método helper getRolNombre()
            return user != null ? user.getRolNombre() : null;
        } catch (Exception e) {
            log.error("Error al obtener rol del usuario {}: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si un usuario está activo
     * @param userId ID del usuario
     * @return true si el usuario está activo, false en caso contrario
     */
    public boolean isUserActive(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            if (user == null) {
                return false;
            }

            // CAMBIO: Usar el método helper getEstadoNombre()
            String estadoNombre = user.getEstadoNombre();
            return "ACTIVO".equalsIgnoreCase(estadoNombre) ||
                    "Activo".equalsIgnoreCase(estadoNombre);
        } catch (Exception e) {
            log.error("Error al verificar estado del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }
}