package com.andre.rinha.features;

import com.andre.rinha.*;
import java.util.Date;

public class GenerateBalanceStatementUseCase {

    private static final int LAST_TRANSACTIONS_LENGTH_LIMIT = 10;

    private final FetchClientPort fetchClient;
    private final FetchExecutedTransactionsPort fetchExecutedTransactions;

    public GenerateBalanceStatementUseCase(FetchClientPort fetchClient,
                                           FetchExecutedTransactionsPort fetchExecutedTransactions) {
        this.fetchClient = fetchClient;
        this.fetchExecutedTransactions = fetchExecutedTransactions;
    }

    public ClientAccountStatement generateStatement(Integer clientId) {
        return fetchClient.fetchById(clientId).map(clientAccount ->
                new ClientAccountStatement(
                        clientAccount.balance(), clientAccount.limit(), new Date(),
                        fetchExecutedTransactions.fetchByClientId(clientId, LAST_TRANSACTIONS_LENGTH_LIMIT)
                )
        ).orElse(null);

    }

}
