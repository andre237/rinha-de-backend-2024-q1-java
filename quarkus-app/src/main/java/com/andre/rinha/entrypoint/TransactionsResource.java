package com.andre.rinha.entrypoint;

import com.andre.rinha.ClientAccount;
import com.andre.rinha.ClientAccountStatement;
import com.andre.rinha.features.GenerateBalanceStatementUseCase;
import com.andre.rinha.features.MakeTransactionUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/clientes")
public class TransactionsResource {

    @Inject MakeTransactionUseCase transactionUseCase;
    @Inject GenerateBalanceStatementUseCase balanceStatementUseCase;

    @POST
    @Path("/{clientId}/transacoes")
    public Response makeTransaction(@PathParam("clientId") Integer clientId,
                                    TransactionRequestDTO transactionRequest) {
        var makeTransactionResult = transactionUseCase.makeTransaction(transactionRequest.toDomainRequest(clientId));

        ClientAccount account = makeTransactionResult.account();

        return switch (makeTransactionResult.result()) {
            case INVALID_REQUEST, LIMIT_EXCEEDED -> Response.status(422).build();
            case CLIENT_NOT_FOUND -> Response.status(404).build();
            case COMPLETED -> Response.status(200).entity(new TransactionResponseDTO(account.limit(), account.balance())).build();
        };
    }

    @GET
    @Path("{clientId}/extrato")
    public Response generateStatement(@PathParam("clientId") Integer clientId) {
        ClientAccountStatement clientAccountStatement = balanceStatementUseCase.generateStatement(clientId);
        return clientAccountStatement != null ?
                Response.status(200).entity(AccountStatementDTO.fromDomain(clientAccountStatement)).build() :
                Response.status(404).build();
    }

}
