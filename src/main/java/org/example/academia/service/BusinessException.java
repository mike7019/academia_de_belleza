package org.example.academia.service;

/**n+ * Excepción para errores de validación/negocio en los servicios.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

