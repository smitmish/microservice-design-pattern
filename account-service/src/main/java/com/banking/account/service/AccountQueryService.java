package com.banking.account.service;

import com.banking.account.entity.AccountSummaryView;

import java.util.Optional;

public interface AccountQueryService {
    Optional<AccountSummaryView> getAccountSummary(Long accountNumber);
}
