package com.andre.rinha;

import java.util.Date;
import java.util.List;

public record ClientAccountStatement(Long balance, Long limit, Date generatedAt, List<TransactionRequest> lastTransactions) {}
