package org.example.academia.util;

/**
 * Excepción para errores de negocio en la aplicación.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

