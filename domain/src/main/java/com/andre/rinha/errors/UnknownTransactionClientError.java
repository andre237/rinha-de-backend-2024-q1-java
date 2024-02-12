package com.andre.rinha.errors;

public class UnknownTransactionClientError extends TransactionExecutionError {

    public UnknownTransactionClientError(String message) {
        super(message);
    }

}
