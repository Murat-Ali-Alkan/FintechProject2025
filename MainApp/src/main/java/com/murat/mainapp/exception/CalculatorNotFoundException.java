package com.murat.mainapp.exception;

public class CalculatorNotFoundException extends RuntimeException {
    public CalculatorNotFoundException(String message) {
        super(message);
    }
}
