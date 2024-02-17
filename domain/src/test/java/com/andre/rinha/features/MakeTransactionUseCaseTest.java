package com.andre.rinha.features;

import com.andre.rinha.*;
import com.andre.rinha.errors.*;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import static com.andre.rinha.BalanceUpdateResult.*;
import static com.andre.rinha.TransactionRequestType.CREDIT;
import static com.andre.rinha.TransactionRequestType.DEBIT;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MakeTransactionUseCaseTest {

    @Mock private RegisterTransactionPort registerTransaction;
    @Mock private UpdateAccountBalancePort updateAccountBalance;
    @Mock private CreateTransactionIsolationPort createTransactionIsolation;

    @InjectMocks
    private MakeTransactionUseCase transactionUseCase;

    @BeforeEach
    void setup() throws TransactionExecutionError {
        lenient().when(createTransactionIsolation.runIsolated(any(), any())).thenAnswer(invocationOnMock -> {
            Supplier<Object> supplier = invocationOnMock.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void shouldThrowInvalidRequestError_OnInvalidRequests() {
        this.assertInvalidRequestError("invalid request value", buildRequest(null, CREDIT, "desc"));
        this.assertInvalidRequestError("invalid request type", buildRequest(100L, null, "desc"));
        this.assertInvalidRequestError("invalid request description", buildRequest(100L, CREDIT, null));
        this.assertInvalidRequestError("invalid request description", buildRequest(100L, CREDIT, ""));
        this.assertInvalidRequestError("invalid request description", buildRequest(100L, CREDIT, "desc too long"));
    }

    @Test
    void shouldThrowUnknownClientError_WhenClientAccountIsNotFound() {
        when(updateAccountBalance.update(any(), any())).thenReturn(Pair.of(CLIENT_NOT_FOUND, null));

        TransactionRequest request = buildRequest(100L, CREDIT, "desc");
        Assertions.assertThatThrownBy(() -> transactionUseCase.makeTransaction(request))
                .isInstanceOf(UnknownTransactionClientError.class)
                .hasMessage("client not found");
    }

    @Test
    void shouldThrowLimitTransactionError_WhenDebitTransactionIsGreaterThanLimit() {
        when(updateAccountBalance.update(any(), any())).thenReturn(Pair.of(LIMIT_EXCEEDED, null));

        TransactionRequest request = buildRequest(160L, DEBIT, "desc");
        Assertions.assertThatThrownBy(() -> transactionUseCase.makeTransaction(request))
                .isInstanceOf(LimitExceededTransactionError.class)
                .hasMessage("not enough balance to fullfil transaction");
    }

    @Test
    void shouldThrowUnexpectedTransactionError_WhenExceptionIsCaught() {
        when(updateAccountBalance.update(any(), any())).thenThrow(IllegalArgumentException.class);
        TransactionRequest request = buildRequest(160L, DEBIT, "desc");
        Assertions.assertThatThrownBy(() -> transactionUseCase.makeTransaction(request))
                .isInstanceOf(UnexpectedTransactionError.class)
                .hasMessage("unexpected error during transaction, contact support");
    }

    @Test
    void shouldCompleteTransaction_WhenLimitIsNotExceeded() throws TransactionExecutionError {
        ClientAccount clientAccount = new ClientAccount(1, 100L, 50L);
        when(updateAccountBalance.update(any(), any())).thenReturn(Pair.of(COMPLETED, clientAccount));

        TransactionRequest request = buildRequest(20L, CREDIT, "desc");
        ClientAccount updatedAccount = transactionUseCase.makeTransaction(request);

        Assertions.assertThat(updatedAccount).isNotNull();
        verify(registerTransaction, times(1)).register(request, clientAccount);
        verify(updateAccountBalance, times(1)).update(clientAccount.id(), 20L);
    }

    private void assertInvalidRequestError(String expectedErrorMessage, TransactionRequest request) {
        Assertions.assertThatThrownBy(() -> transactionUseCase.makeTransaction(request))
                .isInstanceOf(InvalidTransactionRequestError.class)
                .hasMessage(expectedErrorMessage);
    }

    private TransactionRequest buildRequest(Long value, TransactionRequestType type, String description) {
        return new TransactionRequest(1, value, type, description, new Date());
    }

}
