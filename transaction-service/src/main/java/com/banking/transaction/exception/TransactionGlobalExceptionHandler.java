package com.banking.transaction.exception;

import com.banking.transaction.dto.ErrorDetailsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class TransactionGlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionGlobalExceptionHandler.class);
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity handleTransactionException(TransactionException exception, WebRequest webRequest) {
        LOGGER.error("Business rule violation: ", exception);
        ErrorDetailsDto error = new ErrorDetailsDto(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleGenericException(Exception exception, WebRequest webRequest) {
        LOGGER.error("Unhandled technical system failure: ", exception);
        ErrorDetailsDto error = new ErrorDetailsDto(
                LocalDateTime.now(),
                exception.getMessage() != null ? exception.getMessage() : "An unexpected system error occurred",
                webRequest.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}