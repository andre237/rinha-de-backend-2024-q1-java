package com.andre.rinha.errors;

public class InvalidTransactionRequestError extends TransactionExecutionError {

    public InvalidTransactionRequestError(String message) {
        super(message);
    }
}
