package com.andre.rinha.spring;

import com.andre.rinha.features.GenerateBalanceStatementUseCase;
import com.andre.rinha.features.MakeTransactionUseCase;
import com.andre.rinha.spring.adapter.jdbc.ClientAccountRepositoryAdapter;
import com.andre.rinha.spring.adapter.jdbc.PostgresApplicationLockAdapter;
import com.andre.rinha.spring.adapter.jdbc.TransactionsRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringBeansConfiguration {

    @Bean
    public MakeTransactionUseCase makeTransactionUseCase(
            PostgresApplicationLockAdapter transactionIsolation,
            ClientAccountRepositoryAdapter clientAccountRepository,
            TransactionsRepositoryAdapter transactionRegistrationRepository
    ) {
        return new MakeTransactionUseCase(
                clientAccountRepository, transactionRegistrationRepository,
                clientAccountRepository, transactionIsolation);
    }

    @Bean
    public GenerateBalanceStatementUseCase generateBalanceStatementUseCase(
            ClientAccountRepositoryAdapter clientAccountRepository,
            TransactionsRepositoryAdapter transactionRegistrationRepository) {
        return new GenerateBalanceStatementUseCase(clientAccountRepository, transactionRegistrationRepository);
    }

}
