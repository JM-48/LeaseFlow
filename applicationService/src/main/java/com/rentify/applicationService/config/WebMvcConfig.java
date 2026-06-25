package com.rentify.applicationService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra el ApiKeyInterceptor sobre todos los endpoints de negocio (/api/**).
 *
 * Se excluyen explícitamente las rutas de Swagger/OpenAPI y Actuator porque
 * son herramientas de documentación/monitoreo, no datos de negocio, y no
 * tiene sentido protegerlas con la misma llave que usa el Frontend.
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
                        "/actuator/**"
                );
    }
}