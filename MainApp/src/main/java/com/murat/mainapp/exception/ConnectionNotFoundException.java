package com.murat.mainapp.exception;

/**
 * Exception thrown when a connection cannot be established.
 */
public class ConnectionNotFoundException extends RuntimeException {
    /**
     * Constructs a new {@code CalculatorNotFoundException} with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public ConnectionNotFoundException(String message) {
        super(message);
    }
}
