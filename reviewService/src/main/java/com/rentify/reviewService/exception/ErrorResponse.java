package com.rentify.reviewService.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Clase para estructurar respuestas de error en formato JSON.
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