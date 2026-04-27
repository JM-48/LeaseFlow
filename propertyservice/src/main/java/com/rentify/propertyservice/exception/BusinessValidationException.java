package com.rentify.propertyservice.exception;

/**
 * Excepción lanzada cuando hay errores de validación de negocio.
 * Mapea a HTTP 400 BAD_REQUEST.
 */
public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) {
        super(message);
    }
}