package com.andre.rinha.errors;

public class UnexpectedTransactionError extends TransactionExecutionError {

    public UnexpectedTransactionError(Exception cause) {
        super("unexpected error during transaction, contact support", cause);
    }
}
