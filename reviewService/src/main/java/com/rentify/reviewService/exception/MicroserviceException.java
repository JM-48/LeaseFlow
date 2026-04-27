package com.rentify.reviewService.exception;

/**
 * Excepción lanzada cuando falla la comunicación con otro microservicio.
 * Retorna HTTP 503 SERVICE UNAVAILABLE.
 */
public class MicroserviceException extends RuntimeException {

    public MicroserviceException(String message) {
        super(message);
    }
}