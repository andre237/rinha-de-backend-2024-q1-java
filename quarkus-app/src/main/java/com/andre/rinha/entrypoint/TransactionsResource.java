package com.andre.rinha.entrypoint;

import com.andre.rinha.ClientAccount;
import com.andre.rinha.ClientAccountStatement;
import com.andre.rinha.errors.UnknownTransactionClientError;
import com.andre.rinha.features.GenerateBalanceStatementUseCase;
import com.andre.rinha.features.MakeTransactionUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/clientes")
public class TransactionsResource {

    @Inject MakeTransactionUseCase transactionUseCase;
    @Inject GenerateBalanceStatementUseCase balanceStatementUseCase;

    @POST
    @Path("/{clientId}/transacoes")
    public TransactionResponseDTO makeTransaction(@PathParam("clientId") Integer clientId,
                                                  TransactionRequestDTO transactionRequest) {
        ClientAccount clientAccount = transactionUseCase.makeTransaction(transactionRequest.toDomainRequest(clientId));
        return new TransactionResponseDTO(clientAccount.limit(), clientAccount.balance());
    }

    @GET
    @Path("{clientId}/extrato")
    public AccountStatementDTO generateStatement(
            @PathParam("clientId") Integer clientId
    ) throws UnknownTransactionClientError {
        ClientAccountStatement clientAccountStatement = balanceStatementUseCase.generateStatement(clientId);
        return AccountStatementDTO.fromDomain(clientAccountStatement);
    }

}
