package com.andre.rinha.features;

import com.andre.rinha.*;
import com.andre.rinha.errors.UnknownTransactionClientError;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.instancio.Instancio;

import java.util.List;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateBalanceStatementUseCaseTest {

    @Mock private FetchClientPort fetchClient;
    @Mock private FetchExecutedTransactionsPort fetchExecutedTransactions;

    @InjectMocks
    private GenerateBalanceStatementUseCase statementUseCase;

    @Test
    void shouldThrowUnknownClientError_WhenClientAccountIsNotFound() {
        final int unkwownClientId = 19;
        when(fetchClient.fetchById(any())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> statementUseCase.generateStatement(unkwownClientId))
                .isInstanceOf(UnknownTransactionClientError.class)
                .hasMessage("client not found");
    }

    @Test
    void shouldHaveEmptyTransactionList_WhenNoTransactionMadeYet() throws UnknownTransactionClientError {
        final int clientId = 1;
        ClientAccount clientAccount = Instancio.of(ClientAccount.class).set(field(ClientAccount::id), clientId).create();
        when(fetchClient.fetchById(any())).thenReturn(Optional.of(clientAccount));

        ClientAccountStatement statement = statementUseCase.generateStatement(clientId);
        Assertions.assertThat(statement).isNotNull();
        Assertions.assertThat(statement.balance()).isNotNull();
        Assertions.assertThat(statement.lastTransactions()).isEmpty();
    }

    @Test
    void shouldHaveListWith5Transactions_AndCorrespodingBalance() throws UnknownTransactionClientError {
        final int clientId = 1;
        ClientAccount clientAccount = Instancio.of(ClientAccount.class).set(field(ClientAccount::id), clientId).create();
        when(fetchClient.fetchById(any())).thenReturn(Optional.of(clientAccount));

        when(fetchExecutedTransactions.fetchByClientId(any(), any())).thenReturn(mockTransactionList());

        ClientAccountStatement statement = statementUseCase.generateStatement(clientId);
        Assertions.assertThat(statement).isNotNull();
        Assertions.assertThat(statement.balance()).isNotNull().isEqualTo(clientAccount.balance());
        Assertions.assertThat(statement.lastTransactions()).hasSize(5);
    }

    private List<TransactionRequest> mockTransactionList() {
        return Instancio.ofList(TransactionRequest.class)
                .size(5)
                .set(field(TransactionRequest::clientId), 1)
                .create();
    }


}
