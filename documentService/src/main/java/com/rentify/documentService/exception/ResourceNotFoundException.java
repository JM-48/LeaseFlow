package com.rentify.documentService.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe.
 * Mapea a HTTP 404 NOT FOUND.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}