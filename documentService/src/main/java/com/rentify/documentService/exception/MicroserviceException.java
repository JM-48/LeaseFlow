package com.rentify.documentService.exception;

/**
 * Excepción lanzada cuando hay un error en la comunicación con otro microservicio.
 * Mapea a HTTP 503 SERVICE UNAVAILABLE.
 */
public class MicroserviceException extends RuntimeException {
    public MicroserviceException(String message) {
        super(message);
    }
}