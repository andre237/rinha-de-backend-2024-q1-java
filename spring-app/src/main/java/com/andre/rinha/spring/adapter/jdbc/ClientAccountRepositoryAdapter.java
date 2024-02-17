package com.andre.rinha.spring.adapter.jdbc;

import com.andre.rinha.BalanceUpdateResult;
import com.andre.rinha.ClientAccount;
import com.andre.rinha.FetchClientPort;
import com.andre.rinha.UpdateAccountBalancePort;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Component
public class ClientAccountRepositoryAdapter implements FetchClientPort, UpdateAccountBalancePort {

    private final JdbcTemplate jdbcTemplate;

    public ClientAccountRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ClientAccount> fetchById(Integer clientId) {
        final String sql = "select id, \"limit\", balance from client_account where id = ?";

        return jdbcTemplate.query(sql, new AccountTableMapper(), clientId)
                .stream().findAny();
    }

    @Override
    public Pair<BalanceUpdateResult, ClientAccount> update(Integer clientId, Long updateValue) {
        try {
            final String sql = "update client_account set balance = balance + ? where id = ? returning id, \"limit\", balance;";
            ClientAccount clientAccount = jdbcTemplate.queryForObject(sql, new AccountTableMapper(), updateValue, clientId);
            return Pair.of(BalanceUpdateResult.COMPLETED, clientAccount);
        } catch (EmptyResultDataAccessException clientNotFound) {
            return Pair.of(BalanceUpdateResult.CLIENT_NOT_FOUND, null);
        } catch (DataIntegrityViolationException limitExceeded) {
            return Pair.of(BalanceUpdateResult.LIMIT_EXCEEDED, null);
        }
    }

    private static class AccountTableMapper implements RowMapper<ClientAccount> {
        @Override
        public ClientAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt(1);
            long accountLimit = rs.getLong(2);
            long accountBalance = rs.getLong(3);

            return new ClientAccount(id, accountLimit, accountBalance);
        }
    }

}
