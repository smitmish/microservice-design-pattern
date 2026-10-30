package com.banking.account.exception;

public class AccountNotFoundException extends AccountException{
    public AccountNotFoundException(String message) {
        super(message);
    }
}
