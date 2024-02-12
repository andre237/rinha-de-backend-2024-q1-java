package com.andre.rinha.spring.entrypoint;


import com.andre.rinha.ClientAccount;
import com.andre.rinha.errors.TransactionExecutionError;
import com.andre.rinha.features.MakeTransactionUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("clientes")
public class TransactionsController {

    private final MakeTransactionUseCase transactionUseCase;

    public TransactionsController(MakeTransactionUseCase transactionUseCase) {
        this.transactionUseCase = transactionUseCase;
    }

    @PostMapping("{clientId}/transacoes")
    public ClientAccount makeTransaction(
            @PathVariable Integer clientId,
            @RequestBody TransactionRequestDTO requestDTO
    ) throws TransactionExecutionError {
        return transactionUseCase.makeTransaction(requestDTO.toDomainRequest(clientId));
    }

}
