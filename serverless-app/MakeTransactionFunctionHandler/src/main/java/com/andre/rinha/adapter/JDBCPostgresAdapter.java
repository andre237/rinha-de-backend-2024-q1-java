package com.andre.rinha.adapter;

import com.andre.rinha.*;
import org.apache.commons.lang3.tuple.Pair;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class JDBCPostgresAdapter implements RegisterTransactionPort, UpdateAccountBalancePort {

    private final Connection dbConnection;

    public JDBCPostgresAdapter(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void register(TransactionRequest transactionRequest, ClientAccount account) {
        final String sql = "insert into transaction_requests(value, type, description, created_at, account_id) VALUES (?,?,?,?,?);";
        try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)) {
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

    @Override
    public Pair<BalanceUpdateResult, ClientAccount> update(Integer clientId, Long updateValue) {
        final String sql = "update client_account set balance = balance + ? where id = ? returning id, \"limit\", balance;";
        try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)) {
            preparedStatement.setLong(1, updateValue);
            preparedStatement.setInt(2, clientId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    long accountLimit = rs.getLong(2);
                    long accountBalance = rs.getLong(3);

                    return Pair.of(BalanceUpdateResult.COMPLETED, new ClientAccount(id, accountLimit, accountBalance));
                }

                return Pair.of(BalanceUpdateResult.CLIENT_NOT_FOUND, null);
            }

        } catch (SQLException e) {
            if (e instanceof PSQLException && e.getMessage().contains("client_account_v2_check")) {
                return Pair.of(BalanceUpdateResult.LIMIT_EXCEEDED, null);
            }

            throw new RuntimeException(e);
        }
    }
}
