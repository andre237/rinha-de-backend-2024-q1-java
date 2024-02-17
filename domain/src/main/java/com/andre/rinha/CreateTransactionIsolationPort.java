package com.andre.rinha;

import java.util.function.Supplier;

public interface CreateTransactionIsolationPort {

    /**
     * <p>This method must create some form of isolation
     * which ensures that concurrent transactions don't result in
     * inconsistent account balance</p>
     *
     * <p>It should also ensures that the combination of steps to
     * execute a transaction is atomic and reversible in case of failures</p>
     *
     * @param clientId client account which is making the transaction
     * @param supplier code to execute to make the transaction
     * @return generic value provided the supplier method
     */
    <T> T runIsolated(Integer clientId, Supplier<T> supplier);


}
