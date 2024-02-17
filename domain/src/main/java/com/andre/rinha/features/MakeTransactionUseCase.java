package com.andre.rinha.features;

import com.andre.rinha.*;
import com.andre.rinha.errors.*;
import org.apache.commons.lang3.StringUtils;

import static com.andre.rinha.TransactionRequestType.*;

public class MakeTransactionUseCase {

    private static final Integer DESCRIPTION_LENGTH_LIMIT = 10;

    private final RegisterTransactionPort registerTransaction;
    private final UpdateAccountBalancePort updateAccountBalance;
    private final CreateTransactionIsolationPort createTransactionIsolation;

    public MakeTransactionUseCase(RegisterTransactionPort registerTransaction,
                                  UpdateAccountBalancePort updateAccountBalance,
                                  CreateTransactionIsolationPort createTransactionIsolation) {
        this.registerTransaction = registerTransaction;
        this.updateAccountBalance = updateAccountBalance;
        this.createTransactionIsolation = createTransactionIsolation;
    }

    public ClientAccount makeTransaction(TransactionRequest request) throws TransactionExecutionError {
        this.validateOrThrow(request);

        return this.createTransactionIsolation.runIsolated(request.clientId(), () -> {
            try {
                final Long updateValue = request.type().equals(CREDIT) ? +request.value() : -request.value();
                var updateResult = updateAccountBalance.update(request.clientId(), updateValue);

                BalanceUpdateResult resultCode = updateResult.getKey();
                ClientAccount resultAccount = updateResult.getValue();

                switch (resultCode) {
                    case CLIENT_NOT_FOUND -> throw new UnknownTransactionClientError();
                    case LIMIT_EXCEEDED -> throw new LimitExceededTransactionError();
                    case COMPLETED -> registerTransaction.register(request, resultAccount);
                }

                return resultAccount;
            } catch (TransactionExecutionError knownError) {
                throw knownError;
            } catch (Exception unknownError) {
                throw new UnexpectedTransactionError(unknownError);
            }
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
