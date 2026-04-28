package com.rentify.applicationService.exception;

// Excepción para errores de validación de negocio
public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) {
        super(message);
    }
}