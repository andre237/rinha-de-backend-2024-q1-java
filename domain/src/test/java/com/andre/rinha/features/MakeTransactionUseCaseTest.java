package com.andre.rinha.features;

import com.andre.rinha.*;
import com.andre.rinha.errors.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

import static com.andre.rinha.TransactionRequestType.CREDIT;
import static com.andre.rinha.TransactionRequestType.DEBIT;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MakeTransactionUseCaseTest {

    @Mock private FetchClientPort fetchClient;
    @Mock private RegisterTransactionPort registerTransaction;
    @Mock private UpdateAccountBalancePort updateAccountBalance;
    @Mock private CreateTransactionIsolationPort createTransactionIsolation;

    @InjectMocks
    private MakeTransactionUseCase transactionUseCase;

    @BeforeEach
    void setup() throws TransactionExecutionError {
        lenient().when(createTransactionIsolation.runIsolated(any(), any())).thenAnswer(invocationOnMock -> {
            ThrowingSupplier<Object> supplier = invocationOnMock.getArgument(1);
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
        when(fetchClient.fetchById(any())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> transactionUseCase.makeTransaction(buildRequest(100L, CREDIT, "desc")))
                .isInstanceOf(UnknownTransactionClientError.class)
                .hasMessage("client not found");
    }

    @Test
    void shouldThrowLimitTransactionError_WhenDebitTransactionIsGreaterThanLimit() {
        ClientAccount clientAccount = new ClientAccount(1, 100L, 50L);
        when(fetchClient.fetchById(any())).thenReturn(Optional.of(clientAccount));

        Assertions.assertThatThrownBy(() -> transactionUseCase.makeTransaction(buildRequest(160L, DEBIT, "desc")))
                .isInstanceOf(LimitExceededTransactionError.class)
                .hasMessage("not enough balance to fullfil transaction");
    }

    @Test
    void shouldCompleteTransaction_WhenLimitIsNotExceeded() throws TransactionExecutionError {
        ClientAccount clientAccount = new ClientAccount(1, 100L, 50L);
        when(fetchClient.fetchById(any())).thenReturn(Optional.of(clientAccount));

        TransactionRequest request = buildRequest(20L, DEBIT, "desc");
        ClientAccount updatedAccount = transactionUseCase.makeTransaction(request);

        Assertions.assertThat(updatedAccount).isNotNull();
        verify(registerTransaction, times(1)).register(request, clientAccount);
        verify(updateAccountBalance, times(1)).update(clientAccount, 30L);
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
