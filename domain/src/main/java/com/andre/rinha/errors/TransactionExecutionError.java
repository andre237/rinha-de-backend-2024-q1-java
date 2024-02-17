package com.andre.rinha.errors;

public class TransactionExecutionError extends RuntimeException {

    public TransactionExecutionError(String message) {
        super(message);
    }

    public TransactionExecutionError(String message, Throwable cause) {
        super(message, cause);
    }
}
