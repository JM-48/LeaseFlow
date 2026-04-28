package com.rentify.applicationService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuración de CORS para permitir peticiones desde el frontend React
 * Permite conexión con la aplicación frontend en puerto 5173 (Vite)
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir credenciales (cookies, headers de autorización, etc.)
        config.setAllowCredentials(true);

        // Orígenes permitidos - Frontend React con Vite
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite dev server
                "http://localhost:3000",  // Create React App (alternativo)
                "http://localhost:4173"   // Vite preview
        ));

        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
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

        // Headers expuestos al cliente
        config.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Content-Type"
        ));

        // Tiempo de caché de la configuración CORS (en segundos)
        config.setMaxAge(3600L);

        // Aplicar configuración a todos los endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}