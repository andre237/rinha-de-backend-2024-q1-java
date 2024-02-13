package com.andre.rinha.spring.entrypoint;

import com.andre.rinha.ClientAccount;
import com.andre.rinha.ClientAccountStatement;
import com.andre.rinha.errors.TransactionExecutionError;
import com.andre.rinha.errors.UnknownTransactionClientError;
import com.andre.rinha.features.GenerateBalanceStatementUseCase;
import com.andre.rinha.features.MakeTransactionUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("clientes")
public class TransactionsController {

    private final MakeTransactionUseCase transactionUseCase;
    private final GenerateBalanceStatementUseCase balanceStatementUseCase;

    public TransactionsController(MakeTransactionUseCase transactionUseCase,
                                  GenerateBalanceStatementUseCase balanceStatementUseCase) {
        this.transactionUseCase = transactionUseCase;
        this.balanceStatementUseCase = balanceStatementUseCase;
    }

    @PostMapping("{clientId}/transacoes")
    public TransactionResponseDTO makeTransaction(
            @PathVariable Integer clientId,
            @RequestBody TransactionRequestDTO requestDTO
    ) throws TransactionExecutionError {
        ClientAccount clientAccount = transactionUseCase.makeTransaction(requestDTO.toDomainRequest(clientId));
        return new TransactionResponseDTO(clientAccount.limit(), clientAccount.balance());
    }

    @GetMapping("{clientId}/extrato")
    public AccountStatementDTO generateStatement(
            @PathVariable Integer clientId
    ) throws UnknownTransactionClientError {
        ClientAccountStatement clientAccountStatement = balanceStatementUseCase.generateStatement(clientId);
        return AccountStatementDTO.fromDomain(clientAccountStatement);
    }

}
