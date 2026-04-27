package com.rentify.propertyservice.exception;

/**
 * Excepción lanzada cuando hay errores de comunicación con otros microservicios.
 * Mapea a HTTP 503 SERVICE_UNAVAILABLE.
 */
public class MicroserviceException extends RuntimeException {
    public MicroserviceException(String message) {
        super(message);
    }
}