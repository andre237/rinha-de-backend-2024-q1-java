package com.andre.rinha.spring.entrypoint;


import com.andre.rinha.TransactionRequest;
import com.andre.rinha.TransactionRequestType;

import java.util.Date;

// TODO validate valor is not double
public record TransactionRequestDTO(Long valor, String tipo, String descricao, Date realizadaEm) {

    TransactionRequest toDomainRequest(Integer clientId) {
        return new TransactionRequest(clientId, valor, TransactionRequestType.fromSymbol(tipo), descricao, null);
    }

    static TransactionRequestDTO fromDomainRequest(TransactionRequest request) {
        return new TransactionRequestDTO(request.value(), request.type().getSymbol(), request.description(), request.createdAt());
    }



}
