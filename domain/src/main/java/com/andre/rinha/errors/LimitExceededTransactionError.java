package com.andre.rinha.errors;

public class LimitExceededTransactionError extends TransactionExecutionError {

    public LimitExceededTransactionError() {
        super("not enough balance to fullfil transaction");
    }

}
