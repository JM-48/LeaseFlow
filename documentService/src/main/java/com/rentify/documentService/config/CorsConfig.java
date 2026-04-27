package com.rentify.documentService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing) para Document Service.
 * Permite que el frontend React (puerto 5173) pueda comunicarse con el backend (puerto 8083).
 *
 * IMPORTANTE: Esta configuración es CRÍTICA para que el frontend pueda hacer peticiones
 * al backend sin errores de CORS.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permitir credenciales (cookies, headers de autenticación)
        config.setAllowCredentials(true);

        // Orígenes permitidos (frontend React en diferentes configuraciones)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite dev server (por defecto)
                "http://localhost:3000",  // Create React App (alternativo)
                "http://localhost:4173"   // Vite preview mode
        ));

        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Requested-With"
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

        // Tiempo de caché para peticiones preflight (OPTIONS)
        config.setMaxAge(3600L);

        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}