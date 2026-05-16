package com.rentify.userservice.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado
 * Ejemplo: Usuario no encontrado, Rol no encontrado
 * HTTP Status: 404 NOT FOUND
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}