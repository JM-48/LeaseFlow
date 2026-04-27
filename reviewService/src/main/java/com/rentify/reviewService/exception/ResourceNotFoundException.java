package com.rentify.reviewService.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en la base de datos.
 * Retorna HTTP 404 NOT FOUND.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}