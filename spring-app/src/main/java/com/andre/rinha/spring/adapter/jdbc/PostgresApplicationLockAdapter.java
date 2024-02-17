package com.andre.rinha.spring.adapter.jdbc;

import com.andre.rinha.CreateTransactionIsolationPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class PostgresApplicationLockAdapter implements CreateTransactionIsolationPort {

    private final TransactionTemplate transactionTemplate;

    public PostgresApplicationLockAdapter(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T runIsolated(Integer clientId, Supplier<T> supplier) {
        return transactionTemplate.execute(status -> supplier.get());
    }
}
