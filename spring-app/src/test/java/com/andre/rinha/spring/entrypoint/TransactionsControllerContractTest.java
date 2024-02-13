package com.andre.rinha.spring.entrypoint;

import com.andre.rinha.ClientAccount;
import com.andre.rinha.features.GenerateBalanceStatementUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.andre.rinha.errors.InvalidTransactionRequestError;
import com.andre.rinha.errors.UnknownTransactionClientError;
import com.andre.rinha.features.MakeTransactionUseCase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionsController.class)
public class TransactionsControllerContractTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private MakeTransactionUseCase transactionUseCase;
    @MockBean private GenerateBalanceStatementUseCase balanceStatementUseCase;

    @Test
    void shouldReturnStatus422_WhenRequestValidationFails() throws Exception {
        final int existingClient = 2;
        final var request = new TransactionRequestDTO(1000L, "G", "description", new Date());

        when(transactionUseCase.makeTransaction(any())).thenThrow(InvalidTransactionRequestError.class);

        mockMvc.perform(post("/clientes/{client}/transacoes", existingClient)
                .contentType("application/json")
                .content(writeJson(request))
        ).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturnStatus404_WhenClientIsNotFound() throws Exception {
        final int notFoundClient = 9;
        final var request = new TransactionRequestDTO(1000L, "C", "description", new Date());

        when(transactionUseCase.makeTransaction(any())).thenThrow(UnknownTransactionClientError.class);

        mockMvc.perform(post("/clientes/{client}/transacoes", notFoundClient)
                .contentType("application/json")
                .content(writeJson(request))
        ).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStatus200_WhenTransactionIsCompleted() throws Exception {
        final int existingClient = 1;
        final var request = new TransactionRequestDTO(1000L, "G", "description", new Date());

        when(transactionUseCase.makeTransaction(any())).thenReturn(new ClientAccount(1, 1000L, 900L));

        mockMvc.perform(post("/clientes/{client}/transacoes", existingClient)
                .contentType("application/json")
                .content(writeJson(request))
        ).andExpect(status().isOk());
    }

    private String writeJson(Object any) throws JsonProcessingException {
        return objectMapper.writeValueAsString(any);
    }


}
