package com.murat.mainapp.exception;

/**
 * Exception thrown when a requested current cannot be found.
 */
public class CurrencyNotFoundException extends RuntimeException {
    /**
     * Constructs a new {@code CalculatorNotFoundException} with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public CurrencyNotFoundException(String message) {
        super(message);
    }
}
