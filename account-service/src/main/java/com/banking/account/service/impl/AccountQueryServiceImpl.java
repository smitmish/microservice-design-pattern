package com.banking.account.service.impl;

import com.banking.account.entity.AccountSummaryView;
import com.banking.account.repository.AccountQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountQueryServiceImpl {

    @Autowired
    private AccountQueryRepository accountQueryRepository;

    @Transactional(readOnly = true)
    public Optional<AccountSummaryView> getAccountSummary(Long accountNumber) {
        return accountQueryRepository.findByAccountNumber(accountNumber);
    }
}
