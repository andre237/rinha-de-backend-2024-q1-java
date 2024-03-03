package com.andre.rinha.features;

import com.andre.rinha.*;
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

    public MakeTransactionResult makeTransaction(TransactionRequest request) {
        if (!isValidRequest(request)) {
            return new MakeTransactionResult(BalanceUpdateResult.INVALID_REQUEST, null);
        }

        return this.createTransactionIsolation.runIsolated(request.clientId(), () -> {
            final Long updateValue = request.type().equals(CREDIT) ? +request.value() : -request.value();
            var updateResult = updateAccountBalance.update(request.clientId(), updateValue);

            BalanceUpdateResult resultCode = updateResult.getKey();
            ClientAccount resultAccount = updateResult.getValue();

            if (BalanceUpdateResult.COMPLETED.equals(resultCode)) {
                registerTransaction.register(request, resultAccount);
            }

            return new MakeTransactionResult(resultCode, resultAccount);
        });
    }

    private boolean isValidRequest(TransactionRequest request) {
        if (request.value() == null || request.value() <= 0)
            return false;

        if (request.type() == null)
            return false;

        return !StringUtils.isEmpty(request.description()) && request.description().length() <= DESCRIPTION_LENGTH_LIMIT;
    }

}
