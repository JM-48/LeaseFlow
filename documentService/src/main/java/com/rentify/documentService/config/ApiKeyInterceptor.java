package com.rentify.documentService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${app.security.client-key}")
    private String expectedClientKey;

    private static final String HEADER_NAME = "X-App-Client";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        String receivedKey = request.getHeader(HEADER_NAME);

        if (receivedKey == null || receivedKey.isBlank() || !receivedKey.equals(expectedClientKey)) {
            log.warn("Petición rechazada por falta/mismatch de {}. Path: {}, Método: {}, IP: {}",
                    HEADER_NAME, request.getRequestURI(), request.getMethod(), request.getRemoteAddr());

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"timestamp\":\"" + java.time.LocalDateTime.now() + "\","
                            + "\"status\":403,"
                            + "\"error\":\"Forbidden\","
                            + "\"message\":\"Acceso directo no permitido. Esta API requiere un cliente autorizado.\"}"
            );
            return false;
        }

        return true;
    }
}