package com.andre.rinha.features;

import com.andre.rinha.*;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.function.Supplier;

import static com.andre.rinha.BalanceUpdateResult.*;
import static com.andre.rinha.TransactionRequestType.CREDIT;
import static com.andre.rinha.TransactionRequestType.DEBIT;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
class MakeTransactionUseCaseTest {

    @Mock private RegisterTransactionPort registerTransaction;
    @Mock private UpdateAccountBalancePort updateAccountBalance;
    @Mock private CreateTransactionIsolationPort createTransactionIsolation;

    @InjectMocks
    private MakeTransactionUseCase transactionUseCase;

    @BeforeEach
    void setup() {
        lenient().when(createTransactionIsolation.runIsolated(any(), any())).thenAnswer(invocationOnMock -> {
            Supplier<Object> supplier = invocationOnMock.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void shoulReturnResultOfINVALID_REQUEST_OnInvalidRequests() {
        this.assertTransactionResult(INVALID_REQUEST, buildRequest(null, CREDIT, "desc"));
        this.assertTransactionResult(INVALID_REQUEST, buildRequest(100L, null, "desc"));
        this.assertTransactionResult(INVALID_REQUEST, buildRequest(100L, CREDIT, null));
        this.assertTransactionResult(INVALID_REQUEST, buildRequest(100L, CREDIT, ""));
        this.assertTransactionResult(INVALID_REQUEST, buildRequest(100L, CREDIT, "desc too long"));
    }

    @Test
    void shouldReturnResultOfCLIENT_NOT_FOUND_WhenClientAccountIsNotFound() {
        when(updateAccountBalance.update(any(), any())).thenReturn(Pair.of(CLIENT_NOT_FOUND, null));
        this.assertTransactionResult(CLIENT_NOT_FOUND, buildRequest(100L, CREDIT, "desc"));
    }

    @Test
    void shouldThrowLimitTransactionError_WhenDebitTransactionIsGreaterThanLimit() {
        when(updateAccountBalance.update(any(), any())).thenReturn(Pair.of(LIMIT_EXCEEDED, null));
        this.assertTransactionResult(LIMIT_EXCEEDED, buildRequest(160L, DEBIT, "desc"));
    }

    @Test
    void shouldCompleteTransaction_WhenLimitIsNotExceeded() {
        ClientAccount clientAccount = new ClientAccount(1, 100L, 50L);
        when(updateAccountBalance.update(any(), any())).thenReturn(Pair.of(COMPLETED, clientAccount));

        TransactionRequest request = buildRequest(20L, CREDIT, "desc");
        MakeTransactionResult result = transactionUseCase.makeTransaction(request);

        Assertions.assertThat(result.result()).isEqualTo(COMPLETED);
        Assertions.assertThat(result.account()).isNotNull();
        verify(registerTransaction, times(1)).register(request, clientAccount);
        verify(updateAccountBalance, times(1)).update(clientAccount.id(), 20L);
    }

    private void assertTransactionResult(BalanceUpdateResult expectedResult, TransactionRequest request) {
        Assertions.assertThat(transactionUseCase.makeTransaction(request))
                .matches(result -> expectedResult.equals(result.result()));
    }

    private TransactionRequest buildRequest(Long value, TransactionRequestType type, String description) {
        return new TransactionRequest(1, value, type, description, new Date());
    }

}
