package com.andre.rinha;

import java.util.function.Supplier;

public interface CreateTransactionIsolationPort {

    <T> T runIsolated(Integer clientId, Supplier<T> supplier);


}
