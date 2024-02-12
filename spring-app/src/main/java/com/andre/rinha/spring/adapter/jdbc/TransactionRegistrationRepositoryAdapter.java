package com.andre.rinha.spring.adapter.jdbc;

import com.andre.rinha.ClientAccount;
import com.andre.rinha.RegisterTransactionPort;
import com.andre.rinha.TransactionRequest;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRegistrationRepositoryAdapter implements RegisterTransactionPort {

    @Override
    public void register(TransactionRequest transactionRequest, ClientAccount account) {

    }

}
