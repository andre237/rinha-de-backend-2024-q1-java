package com.andre.rinha;

import com.andre.rinha.adapter.jdbc.ClientAccountRepositoryAdapter;
import com.andre.rinha.adapter.jdbc.JDBCTransactionAdapter;
import com.andre.rinha.features.GenerateBalanceStatementUseCase;
import com.andre.rinha.features.MakeTransactionUseCase;
import com.andre.rinha.adapter.jdbc.TransactionsRepositoryAdapter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.function.Supplier;

@ApplicationScoped
public class ApplicationInjectionConfig {

    @Produces
    public MakeTransactionUseCase makeTransactionUseCase(TransactionsRepositoryAdapter transactionsRepositoryAdapter,
                                                         ClientAccountRepositoryAdapter clientAccountRepositoryAdapter,
                                                         JDBCTransactionAdapter jdbcTransactionAdapter) {
        return new MakeTransactionUseCase(transactionsRepositoryAdapter, clientAccountRepositoryAdapter, jdbcTransactionAdapter);
    }

    @Produces
    public GenerateBalanceStatementUseCase generateBalanceStatementUseCase(
            TransactionsRepositoryAdapter transactionsRepositoryAdapter,
            ClientAccountRepositoryAdapter clientAccountRepositoryAdapter) {
        return new GenerateBalanceStatementUseCase(clientAccountRepositoryAdapter, transactionsRepositoryAdapter);
    }

}
