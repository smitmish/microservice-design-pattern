package com.banking.transaction.dto;

import java.math.BigDecimal;

public record TransactionEventDto(
        String transactionUuid,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        BigDecimal sourceBalanceBefore,
        BigDecimal sourceBalanceAfter,
        BigDecimal targetBalanceBefore,
        BigDecimal targetBalanceAfter,
        String transactionType,   // DEBIT / CREDIT / TRANSFER
        String status,
        String referenceNote,
        String location,
        String deviceId,
        Long timestamp
) {}
