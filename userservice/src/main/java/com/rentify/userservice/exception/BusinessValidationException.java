package com.rentify.userservice.exception;

/**
 * Excepción lanzada cuando falla una validación de negocio
 * Ejemplo: Email duplicado, edad insuficiente, rol inválido
 * HTTP Status: 400 BAD REQUEST
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}