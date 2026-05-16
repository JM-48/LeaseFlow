package com.rentify.userservice.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Clase para respuestas de error estandarizadas
 * Incluye timestamp, status HTTP, tipo de error y detalles
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> validationErrors;
}