package com.andre.rinha.adapter.jdbc;

import com.andre.rinha.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Singleton
public class TransactionsRepositoryAdapter implements RegisterTransactionPort, FetchExecutedTransactionsPort {

    @Inject
    DataSource dataSource;

    @Override
    public List<TransactionRequest> fetchByClientId(Integer clientId, Integer limit) {
        final String sql = "select * from transaction_requests where account_id = ? order by created_at desc limit ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)
        ) {

            preparedStatement.setInt(1, clientId);
            preparedStatement.setLong(2, limit);

            try (preparedStatement; ResultSet rs = preparedStatement.executeQuery()) {
                List<TransactionRequest> result = new ArrayList<>();

                while (rs.next()) {
                    long value = rs.getLong(1);
                    String type = rs.getString(2);
                    String description = rs.getString(3);
                    Date createdAt = rs.getTimestamp(4);
                    int accountId = rs.getInt(5);

                    result.add(new TransactionRequest(accountId, value, TransactionRequestType.fromSymbol(type), description, createdAt));
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(TransactionRequest transactionRequest, ClientAccount account) {
        final String sql = "insert into transaction_requests(value, type, description, created_at, account_id) VALUES (?,?,?,?,?);";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, transactionRequest.value());
            preparedStatement.setString(2, transactionRequest.type().getSymbol());
            preparedStatement.setString(3, transactionRequest.description());
            preparedStatement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            preparedStatement.setInt(5, transactionRequest.clientId());

            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
