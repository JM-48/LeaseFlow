package com.rentify.applicationService.client;

import com.rentify.applicationService.dto.UsuarioDTO;
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
public class UserServiceClient {

    private static final String APP_CLIENT_HEADER = "X-App-Client";

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.user-service.url}")
    private String userServiceUrl;

    @Value("${app.security.client-key}")
    private String appClientKey;

    public UsuarioDTO getUserById(Long userId) {
        try {
            log.info("🔍 UserServiceClient: Intentando obtener usuario {} desde URL: {}/api/usuarios/{}",
                    userId, userServiceUrl, userId);

            UsuarioDTO usuario = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/usuarios/" + userId)
                    .header(APP_CLIENT_HEADER, appClientKey)
                    .retrieve()
                    .bodyToMono(UsuarioDTO.class)
                    .timeout(Duration.ofSeconds(15))
                    .onErrorResume(error -> {
                        log.error(" Error HTTP al obtener usuario {}: {} - {}",
                                userId, error.getClass().getSimpleName(), error.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (usuario != null) {
                log.info("✅ Usuario {} encontrado: ID={}, RolId={}, Email={}",
                        userId, usuario.getId(), usuario.getRolId(), usuario.getEmail());
            } else {
                log.warn(" UserServiceClient retornó NULL para usuario {}", userId);
            }

            return usuario;
        } catch (Exception e) {
            log.error(" Error crítico al comunicarse con User Service: {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
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
}