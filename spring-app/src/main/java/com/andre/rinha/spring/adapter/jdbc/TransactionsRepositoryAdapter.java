package com.andre.rinha.spring.adapter.jdbc;

import com.andre.rinha.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class TransactionsRepositoryAdapter implements RegisterTransactionPort, FetchExecutedTransactionsPort {

    private final JdbcTemplate jdbcTemplate;

    public TransactionsRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void register(TransactionRequest transactionRequest, ClientAccount account) {
        final String sql = "insert into transaction_requests(value, type, description, created_at, account_id) VALUES (?,?,?,?,?);";
        jdbcTemplate.update(sql,
                transactionRequest.value(),
                transactionRequest.type().getSymbol(),
                transactionRequest.description(),
                new Date(),
                account.id()
        );
    }

    @Override
    public List<TransactionRequest> fetchByClientId(Integer clientId, Integer limit) {
        final String sql = "select * from transaction_requests where account_id = ? order by created_at desc limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long value = rs.getLong(1);
            String type = rs.getString(2);
            String description = rs.getString(3);
            Date createdAt = rs.getTimestamp(4);
            int accountId = rs.getInt(5);

            return new TransactionRequest(accountId, value, TransactionRequestType.fromSymbol(type), description, createdAt);
        }, clientId, limit);
    }
}
