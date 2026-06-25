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
            log.debug("Consultando usuario con ID: {} en {}", userId, userServiceUrl);

            return webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/usuarios/" + userId + "?includeDetails=true")
                    .header(APP_CLIENT_HEADER, appClientKey)
                    .header(USUARIO_ID_HEADER, String.valueOf(userId))
                    .header(ROL_ID_HEADER, INTERNAL_ROL_ID)
                    .retrieve()
                    .bodyToMono(UsuarioDTO.class)
                    .timeout(Duration.ofSeconds(15))
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

    public boolean existsUser(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            return user != null && user.getId() != null;
        } catch (Exception e) {
            log.error("Error al verificar existencia del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }

    public boolean isAdmin(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            if (user == null) {
                return false;
            }
            String rolNombre = user.getRolNombre();
            return "ADMIN".equalsIgnoreCase(rolNombre);
        } catch (Exception e) {
            log.error("Error al verificar rol de administrador para usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }

    public String getUserRole(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            return user != null ? user.getRolNombre() : null;
        } catch (Exception e) {
            log.error("Error al obtener rol del usuario {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public boolean isUserActive(Long userId) {
        try {
            UsuarioDTO user = getUserById(userId);
            if (user == null) {
                return false;
            }
            String estadoNombre = user.getEstadoNombre();
            return "ACTIVO".equalsIgnoreCase(estadoNombre) ||
                    "Activo".equalsIgnoreCase(estadoNombre);
        } catch (Exception e) {
            log.error("Error al verificar estado del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }
}