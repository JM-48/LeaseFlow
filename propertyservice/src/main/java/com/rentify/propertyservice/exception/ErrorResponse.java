package com.rentify.propertyservice.exception;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Clase que representa la estructura de respuesta de error.
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