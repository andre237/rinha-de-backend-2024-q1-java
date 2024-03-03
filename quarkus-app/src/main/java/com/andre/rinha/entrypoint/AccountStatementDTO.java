package com.andre.rinha.entrypoint;

import com.andre.rinha.ClientAccountStatement;

import java.util.Date;
import java.util.List;

public record AccountStatementDTO(Balance saldo, List<TransactionRequestDTO> ultimasTransacoes) {

    static AccountStatementDTO fromDomain(ClientAccountStatement statement) {
        return new AccountStatementDTO(
                new Balance(statement.balance(), statement.limit(), statement.generatedAt()),
                statement.lastTransactions().stream().map(TransactionRequestDTO::fromDomainRequest).toList());
    }

    public record Balance(Long total, Long limite, Date dataExtrato) {}

}
