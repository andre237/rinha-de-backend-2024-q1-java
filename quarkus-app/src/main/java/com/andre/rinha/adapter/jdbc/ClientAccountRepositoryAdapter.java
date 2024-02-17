package com.andre.rinha.adapter.jdbc;

import com.andre.rinha.BalanceUpdateResult;
import com.andre.rinha.ClientAccount;
import com.andre.rinha.FetchClientPort;
import com.andre.rinha.UpdateAccountBalancePort;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.tuple.Pair;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class ClientAccountRepositoryAdapter implements UpdateAccountBalancePort, FetchClientPort {

    @Inject
    DataSource dataSource;

    @Override
    public Optional<ClientAccount> fetchById(Integer clientId) {
        try (Connection conn = dataSource.getConnection()) {
            final String sql = "select * from client_account where id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
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
    public Pair<BalanceUpdateResult, ClientAccount> update(Integer clientId, Long updateValue) {
        try (Connection conn = dataSource.getConnection()) {
            final String sql = "update client_account set balance = balance + ? where id = ? returning id, \"limit\", balance;";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1, updateValue);
            preparedStatement.setInt(2, clientId);

            try (preparedStatement; ResultSet rs = preparedStatement.executeQuery()) {
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
