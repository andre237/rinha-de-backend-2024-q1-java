package com.andre.rinha.adapter;

import com.andre.rinha.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class JDBCPostgresAdapter implements FetchClientPort, FetchExecutedTransactionsPort {

    private final Connection dbConnection;

    public JDBCPostgresAdapter(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<ClientAccount> fetchById(Integer clientId) {
        final String sql = "select * from client_account where id = ?";
        try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)) {
            preparedStatement.setInt(1, clientId);

            try (preparedStatement; ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    long accountLimit = rs.getLong(2);
                    long accountBalance = rs.getLong(3);

                    return Optional.of(new ClientAccount(id, accountLimit, accountBalance));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public List<TransactionRequest> fetchByClientId(Integer clientId, Integer limit) {
        final String sql = "select * from transaction_requests where account_id = ? order by created_at desc limit ?";
        try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)) {
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
}
