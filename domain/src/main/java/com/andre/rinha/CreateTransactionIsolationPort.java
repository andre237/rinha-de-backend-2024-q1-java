package com.andre.rinha;

import com.andre.rinha.errors.TransactionExecutionError;

public interface CreateTransactionIsolationPort {

    <T> T runIsolated(Integer clientId, ThrowingSupplier<T> supplier) throws TransactionExecutionError;


}
