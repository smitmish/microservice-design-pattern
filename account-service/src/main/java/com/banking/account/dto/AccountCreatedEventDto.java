package com.banking.account.dto;

import java.math.BigDecimal;

public record AccountCreatedEventDto(
        Long accountNumber,
        String accountHolderName,
        BigDecimal currentBalance,
        String accountType, // "SAVINGS" or "BUSINESS"

        // Savings-specific fields (null if Business)
        BigDecimal interestRate,
        BigDecimal minimumBalance,

        // Business-specific fields (null if Savings)
        String taxId,
        BigDecimal overdraftLimit
) {}
