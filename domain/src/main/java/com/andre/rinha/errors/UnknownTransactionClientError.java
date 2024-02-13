package com.andre.rinha.errors;

public class UnknownTransactionClientError extends TransactionExecutionError {

    public UnknownTransactionClientError() {
        this("client not found");
    }

    public UnknownTransactionClientError(String message) {
        super(message);
    }

}
