package com.andre.rinha.spring;

import com.andre.rinha.features.MakeTransactionUseCase;
import com.andre.rinha.spring.adapter.jdbc.ClientAccountRepositoryAdapter;
import com.andre.rinha.spring.adapter.jdbc.PostgresApplicationLockAdapter;
import com.andre.rinha.spring.adapter.jdbc.TransactionRegistrationRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringBeansConfiguration {

    @Bean
    public MakeTransactionUseCase makeTransactionUseCase(
            PostgresApplicationLockAdapter transactionIsolation,
            ClientAccountRepositoryAdapter clientAccountRepository,
            TransactionRegistrationRepositoryAdapter transactionRegistrationRepository
    ) {
        return new MakeTransactionUseCase(
                clientAccountRepository,
                transactionRegistrationRepository,
                clientAccountRepository,
                transactionIsolation
        );
    }

}
