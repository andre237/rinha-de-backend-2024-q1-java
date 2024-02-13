package com.andre.rinha;

import java.util.Date;

public record TransactionRequest(Integer clientId,
                                 Long value,
                                 TransactionRequestType type,
                                 String description,
                                 Date createdAt) {
}
