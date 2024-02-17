package com.andre.rinha;

import java.util.Optional;

public interface FetchClientPort {

    /**
     * Will return the account information for the given {@code clientId}
     * <p>If the account is not found, result me be {@link Optional#empty()}</p>
     *
     * @param clientId client id to search the account
     * @return an optional client account
     */
    Optional<ClientAccount> fetchById(Integer clientId);

}
