package com.andre.rinha;

import java.util.List;

public interface FetchExecutedTransactionsPort {

    /**
     * Will search for the last transactions made on behalf of
     * the account owned by {@code clientId}
     *
     * <p>Result list must be sorted on {@link TransactionRequest#createdAt()} descending</p>
     * <p>If no transaction is found, result me be an empty list</p>
     *
     * @param clientId id of the client whose transactions refer to
     * @param limit the max length of the returned list
     * @return a list with up to "limit" transaction objects
     */
    List<TransactionRequest> fetchByClientId(Integer clientId, Integer limit);

}
