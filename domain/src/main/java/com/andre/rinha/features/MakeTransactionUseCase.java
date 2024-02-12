package com.andre.rinha.features;

import com.andre.rinha.*;
import com.andre.rinha.errors.*;
import org.apache.commons.lang3.StringUtils;

import static com.andre.rinha.TransactionRequestType.*;

public class MakeTransactionUseCase {

    private static final Integer DESCRIPTION_LENGTH_LIMIT = 10;

    private final FetchClientPort fetchClient;
    private final RegisterTransactionPort registerTransaction;
    private final UpdateAccountBalancePort updateAccountBalance;
    private final CreateTransactionIsolationPort createTransactionIsolation;

    public MakeTransactionUseCase(FetchClientPort fetchClient,
                                  RegisterTransactionPort registerTransaction,
                                  UpdateAccountBalancePort updateAccountBalance,
                                  CreateTransactionIsolationPort createTransactionIsolation) {
        this.fetchClient = fetchClient;
        this.registerTransaction = registerTransaction;
        this.updateAccountBalance = updateAccountBalance;
        this.createTransactionIsolation = createTransactionIsolation;
    }

    public ClientAccount makeTransaction(TransactionRequest request) throws TransactionExecutionError {
        this.validateOrThrow(request);

        return createTransactionIsolation.runIsolated(request.clientId(), () -> {
            ClientAccount clientAccount = fetchClient.fetchById(request.clientId())
                    .orElseThrow(() -> new UnknownTransactionClientError("client not found"));

            boolean isDebitTransaction = DEBIT.equals(request.type());
            final Long updateValue = isDebitTransaction ? -request.value() : +request.value();

            registerTransaction.register(request, clientAccount);
            Long newBalance = updateAccountBalance.update(clientAccount, updateValue);

            if (newBalance < 0 && Math.abs(newBalance) > clientAccount.limit()) {
                throw new LimitExceededTransactionError("not enough balance to fullfil transaction");
            }

            return new ClientAccount(clientAccount.id(), clientAccount.limit(), newBalance);
        });
    }

    private void validateOrThrow(TransactionRequest request) throws InvalidTransactionRequestError {
        if (request.value() == null || request.value() <= 0)
            throw new InvalidTransactionRequestError("invalid request value");

        if (request.type() == null)
            throw new InvalidTransactionRequestError("invalid request type");

        if (StringUtils.isEmpty(request.description()) || request.description().length() > DESCRIPTION_LENGTH_LIMIT)
            throw new InvalidTransactionRequestError("invalid request description");
    }

}
