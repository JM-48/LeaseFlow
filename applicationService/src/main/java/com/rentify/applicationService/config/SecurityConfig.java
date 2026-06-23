package com.rentify.applicationService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF porque es una API REST
                .authorizeHttpRequests(auth -> auth
                        // Permitimos que la petición pase la barrera inicial de Spring Security
                        // ¡Nuestro Controlador se encargará de exigir los Headers y dar el 401!
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}