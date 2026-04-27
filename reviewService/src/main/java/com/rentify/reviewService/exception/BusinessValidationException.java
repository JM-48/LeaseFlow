package com.rentify.reviewService.exception;

/**
 * Excepción lanzada cuando una validación de regla de negocio falla.
 * Retorna HTTP 400 BAD REQUEST.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}