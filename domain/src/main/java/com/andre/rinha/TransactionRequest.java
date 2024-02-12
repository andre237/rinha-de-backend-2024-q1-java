package com.andre.rinha;

public record TransactionRequest(Integer clientId, Long value, TransactionRequestType type, String description) {}
