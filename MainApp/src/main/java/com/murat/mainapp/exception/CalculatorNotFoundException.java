package com.murat.mainapp.exception;

/**
 * Exception thrown when a requested rate calculator implementation cannot be found.
 */
public class CalculatorNotFoundException extends RuntimeException {
    /**
     * Constructs a new {@code CalculatorNotFoundException} with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public CalculatorNotFoundException(String message) {
        super(message);
    }
}
