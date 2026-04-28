package com.rentify.applicationService.exception;

import java.time.LocalDateTime;
import java.util.Map;

 // Clase para la respuesta de error
@lombok.Getter
@lombok.Setter
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> validationErrors;
}