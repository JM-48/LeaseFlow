package com.rentify.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra el ApiKeyInterceptor sobre todos los endpoints de negocio (/api/**).
 *
 * No se excluyen /api/usuarios ni /api/usuarios/login: la llave de aplicacion
 * (X-App-Client) es independiente de la sesion del usuario, asi que el
 * Frontend la envia tambien en las pantallas de registro y login.
 *
 * Se excluyen Swagger/OpenAPI y Actuator porque son herramientas de
 * documentacion/monitoreo, no datos de negocio.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/actuator/**"
                );
    }
}