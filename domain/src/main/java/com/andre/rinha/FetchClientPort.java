package com.andre.rinha;

import java.util.Optional;

public interface FetchClientPort {

    Optional<ClientAccount> fetchById(Integer clientId);

}
