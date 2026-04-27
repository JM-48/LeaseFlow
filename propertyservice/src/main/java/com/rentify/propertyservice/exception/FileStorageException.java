package com.rentify.propertyservice.exception;

/**
 * Excepción lanzada cuando hay errores en la gestión de archivos.
 * Mapea a HTTP 400 BAD_REQUEST.
 */
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}