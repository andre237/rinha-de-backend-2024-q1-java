package com.andre.rinha;

import org.apache.commons.lang3.tuple.Pair;

public interface UpdateAccountBalancePort {

    Pair<BalanceUpdateResult, ClientAccount> update(Integer clientId, Long updateValue);

}
