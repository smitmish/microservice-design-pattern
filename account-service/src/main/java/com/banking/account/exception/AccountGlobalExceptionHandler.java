package com.banking.account.exception;

import com.banking.account.dto.ErrorDetailsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AccountGlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountGlobalExceptionHandler.class);

    // 1. SPECIFIC: Resource Not Found -> HTTP 404
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity handleAccountNotFound(AccountNotFoundException ex, WebRequest request) {
        LOGGER.error("Resource not found: ", ex);
        ErrorDetailsDto error = new ErrorDetailsDto(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 2. SPECIFIC: Business Logic / Rule Violation -> HTTP 400
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity handleInsufficientBalance(InsufficientBalanceException ex, WebRequest request) {
        LOGGER.error("Business rule violation: ", ex);
        ErrorDetailsDto error = new ErrorDetailsDto(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. FALLBACK DOMAIN: Catch-all for any other AccountException -> HTTP 400
    @ExceptionHandler(AccountException.class)
    public ResponseEntity handleAccountException(AccountException ex, WebRequest request) {
        LOGGER.error("Account domain exception: ", ex);
        ErrorDetailsDto error = new ErrorDetailsDto(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 4. TECHNICAL CATCH-ALL: Uncaught DB/Infrastructure errors -> HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleGenericException(Exception ex, WebRequest request) {
        LOGGER.error("Unhandled technical system failure: ", ex);
        ErrorDetailsDto error = new ErrorDetailsDto(
                LocalDateTime.now(),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected system error occurred",
                request.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
