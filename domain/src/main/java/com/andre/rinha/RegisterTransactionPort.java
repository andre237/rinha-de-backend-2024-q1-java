package com.andre.rinha;

public interface RegisterTransactionPort {

    /**
     * Will register a transaction linked to the client on {@link ClientAccount#id()}
     *
     * @param transactionRequest transaction information
     * @param account account which is registering the transaction
     */
    void register(TransactionRequest transactionRequest, ClientAccount account);

}
