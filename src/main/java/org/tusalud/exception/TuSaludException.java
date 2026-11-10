// src/main/java/org/tusalud/exception/TuSaludException.java
package org.tusalud.exception;

/**
 * Excepción personalizada del dominio clínico TuSalud (Cumple R8).
 * Se lanza ante reglas de negocio incumplidas o valores biométricos biológicamente imposibles.
 */
public class TuSaludException extends Exception {

    public TuSaludException(String message) {
        super(message);
    }

    public TuSaludException(String message, Throwable cause) {
        super(message, cause);
    }
}
