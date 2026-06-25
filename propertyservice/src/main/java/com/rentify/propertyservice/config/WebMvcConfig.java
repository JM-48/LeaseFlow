package com.rentify.propertyservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra el ApiKeyInterceptor sobre todos los endpoints de negocio (/api/**).
 *
 * Se excluyen explícitamente las rutas de Swagger/OpenAPI y Actuator.
 * Las peticiones OPTIONS (preflight CORS) son manejadas directamente en
 * ApiKeyInterceptor.preHandle(), donde se dejan pasar sin validar el header.
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
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/actuator/**"
                );
    }
}