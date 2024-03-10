package com.andre.rinha.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.mutiny.sqlclient.Row;

import java.util.Date;
import java.util.List;

@RegisterForReflection
public record AccountStatementDTO(Balance saldo, List<TransactionRequestDTO> ultimasTransacoes) {

    @RegisterForReflection
    public record Balance(Long total, Long limite, Date dataExtrato) {
        public static Balance fromRow(Row row) {
            return new Balance(
                    row.getLong("balance"),
                    row.getLong("limit"),
                    new Date(row.getLocalDate("data_extrato").toEpochDay())
            );
        }
    }

}
