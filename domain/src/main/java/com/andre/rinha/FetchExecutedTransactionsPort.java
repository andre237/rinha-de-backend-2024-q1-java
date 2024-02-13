package com.andre.rinha;

import java.util.List;

public interface FetchExecutedTransactionsPort {

    List<TransactionRequest> fetchByClientId(Integer clientId, Integer limit);

}
