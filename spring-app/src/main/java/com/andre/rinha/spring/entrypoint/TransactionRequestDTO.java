package com.andre.rinha.spring.entrypoint;


import com.andre.rinha.TransactionRequest;
import com.andre.rinha.TransactionRequestType;

public record TransactionRequestDTO(Long valor, String tipo, String descricao) {

    TransactionRequest toDomainRequest(Integer clientId) {
        TransactionRequestType type = switch (tipo) {
            case "c" -> TransactionRequestType.CREDIT;
            case "d" -> TransactionRequestType.DEBIT;
            default -> null;
        };

        return new TransactionRequest(clientId, valor, type, descricao);
    }



}
