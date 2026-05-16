package com.rentify.userservice.exception;

/**
 * Excepción lanzada cuando falla la autenticación
 * Ejemplo: Credenciales inválidas, cuenta suspendida
 * HTTP Status: 401 UNAUTHORIZED
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}