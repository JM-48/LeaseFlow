package com.rentify.applicationService.exception;

// Excepción para errores de comunicación con microservicios
public class MicroserviceException extends RuntimeException {
    public MicroserviceException(String message) {
        super(message);
    }
}