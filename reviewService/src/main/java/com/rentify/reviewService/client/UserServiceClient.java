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

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private static final String APP_CLIENT_HEADER = "X-App-Client";

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.user-service.url}")
    private String userServiceUrl;

    @Value("${app.security.client-key}")
    private String appClientKey;

    public UsuarioDTO getUserById(Long userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/usuarios/" + userId)
                    .header(APP_CLIENT_HEADER, appClientKey)
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

    public boolean hasRole(Long userId, String rol) {
        try {
            UsuarioDTO user = getUserById(userId);
            return user != null && rol.equals(user.getRol());
        } catch (Exception e) {
            log.error("Error al verificar rol del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }

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