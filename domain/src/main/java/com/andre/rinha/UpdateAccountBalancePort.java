package com.andre.rinha;

import org.apache.commons.lang3.tuple.Pair;

public interface UpdateAccountBalancePort {

    /**
     * <p>Will update the account owned by {@code clientId}
     * by adding the {@code updateValue} to the current balance</p>
     *
     * <p>If no account is found for the given {@code clientId},
     * it should return {@link BalanceUpdateResult#CLIENT_NOT_FOUND} as the pair key
     * and {@code null} for the pair value</p>
     *
     * <p>If an account is found but the new balance will exceed the account limit,
     * it should return {@link BalanceUpdateResult#LIMIT_EXCEEDED} as the pair key
     * and {@code null} for the pair value
     * </p>
     *
     * <p>If the update completes normally,
     * it should return {@link BalanceUpdateResult#COMPLETED} as the pair key
     * and an instance of {@link ClientAccount} with the new updated balance</p>
     *
     * @param clientId id of the client whose account balance will be updated
     * @param updateValue the update value, which can be positive, negative or zero
     * @return pair object containing the operation result and the updated {@link ClientAccount}
     */
    Pair<BalanceUpdateResult, ClientAccount> update(Integer clientId, Long updateValue);

}
