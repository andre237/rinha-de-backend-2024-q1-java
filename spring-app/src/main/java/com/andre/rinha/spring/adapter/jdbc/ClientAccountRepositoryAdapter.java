package com.andre.rinha.spring.adapter.jdbc;

import com.andre.rinha.ClientAccount;
import com.andre.rinha.FetchClientPort;
import com.andre.rinha.UpdateAccountBalancePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class ClientAccountRepositoryAdapter implements FetchClientPort, UpdateAccountBalancePort {

    private final JdbcTemplate jdbcTemplate;

    public ClientAccountRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ClientAccount> fetchById(Integer clientId) {
        final String sql = "select * from client_account where id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int id = rs.getInt(1);
            long accountLimit = rs.getLong(2);
            long accountBalance = rs.getLong(3);

            return new ClientAccount(id, accountLimit, accountBalance);
        }, clientId).stream().findAny();
    }

    @Override
    public void update(ClientAccount account, Long newValue) {
        final String sql = "update client_account set balance = ? where id = ?";
        jdbcTemplate.update(sql, newValue, account.id());
    }

}
