package com.banking.account.service;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.TransactionEventDto;
import com.banking.account.dto.TransferFundDto;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountDto createAccount(AccountDto accountDto);
    AccountDto getAccountByAccountNumber(Long id);
    AccountDto deposit(Long id, BigDecimal amount, String location, String deviceId);
    AccountDto withdrawal(Long id, BigDecimal amount, String location, String deviceId);
    List getAllAccounts();
    void deleteAccount(Long id);
    AccountDto transferFunds(TransferFundDto transferFundDto);

    void rollbackTransfer(TransactionEventDto event);
}