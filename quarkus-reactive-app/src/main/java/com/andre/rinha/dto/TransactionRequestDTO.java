package com.andre.rinha.dto;

import java.util.Date;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.mutiny.sqlclient.Row;

@RegisterForReflection
public record TransactionRequestDTO(Long valor, String tipo, String descricao, Date realizadaEm) {

    public static TransactionRequestDTO fromRow(Row row) {
        if (row.getLong("value") == null) return null;

        return new TransactionRequestDTO(
                row.getLong("value"),
                row.getString("type"),
                row.getString("description"),
                new Date(row.getLocalDate("created_at").toEpochDay())
        );
    }


}
