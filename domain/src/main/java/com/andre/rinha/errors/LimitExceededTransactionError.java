package com.andre.rinha.errors;

public class LimitExceededTransactionError extends TransactionExecutionError {

    public LimitExceededTransactionError(String message) {
        super(message);
    }

}
