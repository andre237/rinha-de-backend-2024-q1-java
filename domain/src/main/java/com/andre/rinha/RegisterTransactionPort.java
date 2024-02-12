package com.andre.rinha;

public interface RegisterTransactionPort {

    void register(TransactionRequest transactionRequest, ClientAccount account);

}
