package com.andre.rinha.spring.adapter.jdbc;

import com.andre.rinha.CreateTransactionIsolationPort;
import com.andre.rinha.ThrowingSupplier;
import com.andre.rinha.errors.TransactionExecutionError;
import com.andre.rinha.errors.UnexpectedTransactionError;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresApplicationLockAdapter implements CreateTransactionIsolationPort {

    private final JdbcTemplate jdbcTemplate;

    public PostgresApplicationLockAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public <T> T runIsolated(Integer clientId, ThrowingSupplier<T> supplier) throws TransactionExecutionError {
        try {
            jdbcTemplate.execute("select pg_advisory_lock(9991, %d)".formatted(clientId));
            jdbcTemplate.execute("begin ;");
            T result = supplier.get();
            jdbcTemplate.execute("commit ;");
            return result;
        } catch (TransactionExecutionError knownError) {
            jdbcTemplate.execute("rollback ;");
            throw knownError;
        } catch (Exception unknownError) {
            jdbcTemplate.execute("rollback ;");
            throw new UnexpectedTransactionError(unknownError);
        } finally {
            jdbcTemplate.execute("select pg_advisory_unlock(9991, %d)".formatted(clientId));
        }
    }
}
