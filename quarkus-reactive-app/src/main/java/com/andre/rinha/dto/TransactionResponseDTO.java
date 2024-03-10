package com.andre.rinha.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.mutiny.sqlclient.Row;

@RegisterForReflection
public record TransactionResponseDTO(Long limite, Long saldo) {

    public static TransactionResponseDTO fromRow(Row row) {
        return new TransactionResponseDTO(
                row.getLong("limit"),
                row.getLong("balance")
        );
    }

}
