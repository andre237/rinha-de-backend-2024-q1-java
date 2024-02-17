package com.andre.rinha.adapter.jdbc;

import com.andre.rinha.CreateTransactionIsolationPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.function.Supplier;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

@ApplicationScoped
public class JDBCTransactionAdapter implements CreateTransactionIsolationPort {

    @Override
    @Transactional(REQUIRES_NEW)
    public <T> T runIsolated(Integer clientId, Supplier<T> supplier) {
        return supplier.get();
    }
}
