// src/main/java/org/tusalud/exception/TuSaludException.java
package org.tusalud.exception;

/**
 * Excepción personalizada del dominio clínico TuSalud (Cumple R8).
 * Se lanza ante reglas de negocio incumplidas o valores biométricos biológicamente imposibles.
 */
public class TuSaludException extends Exception {
    private final int statusCode;

    public TuSaludException(String message) {
        this(message, 400);
    }

    public TuSaludException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public TuSaludException(String message, Throwable cause) {
        this(message, 400, cause);
    }

    public TuSaludException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
