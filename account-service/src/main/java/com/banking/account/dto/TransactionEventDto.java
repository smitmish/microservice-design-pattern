package com.banking.account.dto; // (Adjust package path for transaction-service)

import java.math.BigDecimal;
import java.time.LocalDateTime;

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