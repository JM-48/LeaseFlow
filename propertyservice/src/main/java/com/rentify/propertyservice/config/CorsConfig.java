package com.rentify.propertyservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuración de CORS para Property Service.
 * Permite que el frontend React se comunique con este microservicio.
 *
 * IMPORTANTE: Este CorsFilter se registra como un Bean de Spring y se ejecuta
 * ANTES que el ApiKeyInterceptor (que es un HandlerInterceptor de Spring MVC),
 * garantizando que las peticiones OPTIONS de preflight CORS sean respondidas
 * correctamente sin ser bloqueadas por el interceptor.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir credenciales (cookies, headers de autenticación)
        config.setAllowCredentials(true);

        // Orígenes permitidos (frontend local + producción en Azure)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173",
                "https://leaseflow-web-a3chbkhjcug5bcgc.brazilsouth-01.azurewebsites.net"
        ));

        // Headers permitidos — CRÍTICO: incluir X-App-Client para que el preflight
        // OPTIONS lo apruebe antes de que llegue al ApiKeyInterceptor
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-App-Client",   // ← AGREGADO: requerido por ApiKeyInterceptor
                "X-Usuario-Id",   // ← AGREGADO: requerido por controladores
                "X-Rol-Id"        // ← AGREGADO: requerido por controladores
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Headers que el cliente puede ver en la respuesta
        config.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
        ));

        // Tiempo de caché para la configuración CORS (1 hora)
        config.setMaxAge(3600L);

        // Aplicar configuración a todas las rutas
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}