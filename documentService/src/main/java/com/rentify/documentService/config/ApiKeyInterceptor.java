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

/**
 * Interceptor de seguridad perimetral.
 *
 * Bloquea cualquier petición a /api/** que no incluya el header X-App-Client
 * con el valor secreto configurado en application.properties.
 *
 * EXCEPCIÓN CRÍTICA: Las peticiones OPTIONS (preflight CORS) se dejan pasar
 * siempre sin validar el header. El navegador las envía automáticamente antes
 * de cada request con headers personalizados (como X-App-Client), y en ese
 * momento aún NO incluye el header ? lo está preguntando si puede enviarlo.
 * Si bloqueamos el OPTIONS, el navegador cancela la request real y la web
 * muestra error de CORS aunque el CorsFilter esté configurado correctamente.
 */
@Component
@Slf4j
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${app.security.client-key}")
    private String expectedClientKey;

    private static final String HEADER_NAME = "X-App-Client";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        // CRÍTICO: dejar pasar siempre las peticiones OPTIONS (preflight CORS).
        // El CorsFilter (Servlet Filter, anterior al DispatcherServlet) las responde
        // correctamente; este interceptor no debe interferir.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String receivedKey = request.getHeader(HEADER_NAME);

        if (receivedKey == null || receivedKey.isBlank() || !receivedKey.equals(expectedClientKey)) {
            log.warn("Petición rechazada por falta/mismatch de {}. Path: {}, Metodo: {}, IP: {}",
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