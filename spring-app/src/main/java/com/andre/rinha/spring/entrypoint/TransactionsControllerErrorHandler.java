package com.andre.rinha.spring.entrypoint;


import com.andre.rinha.errors.InvalidTransactionRequestError;
import com.andre.rinha.errors.LimitExceededTransactionError;
import com.andre.rinha.errors.UnknownTransactionClientError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class TransactionsControllerErrorHandler {

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidTransactionRequestError.class)
    protected ResponseEntity<String> handleInvalidRequestError(InvalidTransactionRequestError error) {
        return ResponseEntity.unprocessableEntity().body(error.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(LimitExceededTransactionError.class)
    protected ResponseEntity<String> handleLimitExceededError(LimitExceededTransactionError error) {
        return ResponseEntity.unprocessableEntity().body(error.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UnknownTransactionClientError.class)
    protected ResponseEntity<String> handleClientNotFoundError(UnknownTransactionClientError error) {
        return ResponseEntity.status(404).body(error.getMessage());
    }

}
