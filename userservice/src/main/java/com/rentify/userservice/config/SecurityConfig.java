package com.rentify.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilitado para APIs REST Stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sin estado
                )
                .authorizeHttpRequests(auth -> auth
                        // Permitimos el paso inicial de Spring Security.
                        // La responsabilidad de seguridad y RBAC recaerá en las cabeceras
                        // del API Gateway validadas directamente en los Controladores.
                        .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Componente central para el manejo seguro de credenciales.
     * Permite inyectar 'PasswordEncoder' en tus servicios para aplicar hashing
     * mediante BCrypt robusto a las contraseñas antes de guardarlas en la BD.
     */
    /**
     * Componente central para el manejo seguro de credenciales.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() { // <-- Cambiamos PasswordEncoder por BCryptPasswordEncoder
        return new BCryptPasswordEncoder();
    }
}