package com.rentify.documentService.exception;

/**
 * Excepción lanzada cuando una validación de negocio falla.
 * Mapea a HTTP 400 BAD REQUEST.
 */
public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) {
        super(message);
    }
}