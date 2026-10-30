package com.banking.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDto(
        Long accountId,
        Long customer,
        Long accountNumber,
        String accountType,
        BigDecimal balance,
        String createdBy,
        LocalDateTime createdAt,
        // Savings-specific
        BigDecimal interestRate,
        BigDecimal minimumBalance,
        // Business-specific
        String taxId,
        BigDecimal overdraftLimit
) {}