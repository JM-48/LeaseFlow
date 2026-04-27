package com.rentify.propertyservice.exception;

/**
 * Excepción lanzada cuando un recurso no es encontrado.
 * Mapea a HTTP 404 NOT_FOUND.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}