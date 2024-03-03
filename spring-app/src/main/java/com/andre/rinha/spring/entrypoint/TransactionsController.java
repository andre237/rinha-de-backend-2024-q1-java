package com.andre.rinha.spring.entrypoint;

import com.andre.rinha.ClientAccount;
import com.andre.rinha.ClientAccountStatement;
import com.andre.rinha.MakeTransactionResult;
import com.andre.rinha.features.GenerateBalanceStatementUseCase;
import com.andre.rinha.features.MakeTransactionUseCase;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TransactionResponseDTO> makeTransaction(
            @PathVariable Integer clientId,
            @RequestBody TransactionRequestDTO requestDTO) {
        MakeTransactionResult makeTransactionResult = transactionUseCase
                .makeTransaction(requestDTO.toDomainRequest(clientId));

        ClientAccount account = makeTransactionResult.account();

        return switch (makeTransactionResult.result()) {
            case INVALID_REQUEST, LIMIT_EXCEEDED -> ResponseEntity.unprocessableEntity().build();
            case CLIENT_NOT_FOUND -> ResponseEntity.notFound().build();
            case COMPLETED -> ResponseEntity.ok(new TransactionResponseDTO(account.limit(), account.balance()));
        };
    }

    @GetMapping("{clientId}/extrato")
    public ResponseEntity<AccountStatementDTO> generateStatement(@PathVariable Integer clientId) {
        ClientAccountStatement clientAccountStatement = balanceStatementUseCase.generateStatement(clientId);
        return clientAccountStatement != null ?
                ResponseEntity.ok(AccountStatementDTO.fromDomain(clientAccountStatement)) :
                ResponseEntity.notFound().build();
    }

}
