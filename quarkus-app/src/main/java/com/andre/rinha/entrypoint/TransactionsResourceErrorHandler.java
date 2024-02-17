package com.andre.rinha.entrypoint;

import com.andre.rinha.errors.InvalidTransactionRequestError;
import com.andre.rinha.errors.LimitExceededTransactionError;
import com.andre.rinha.errors.TransactionExecutionError;
import com.andre.rinha.errors.UnknownTransactionClientError;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TransactionsResourceErrorHandler implements ExceptionMapper<TransactionExecutionError> {

    @Override
    public Response toResponse(TransactionExecutionError transactionError) {
        if (InvalidTransactionRequestError.class.isAssignableFrom(transactionError.getClass()) || LimitExceededTransactionError.class.isAssignableFrom(transactionError.getClass())) {
            return Response.status(422).entity(transactionError.getMessage())
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
        } else if (UnknownTransactionClientError.class.isAssignableFrom(transactionError.getClass())) {
            return Response.status(404).entity(transactionError.getMessage())
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
        }

        return Response.status(500).build();
    }
}
