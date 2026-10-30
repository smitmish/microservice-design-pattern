package com.banking.transaction.dto;

import com.banking.transaction.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto(
        Long transactionId,
        String transactionUuid,
        Long sourceAccountId,
        Long targetAccountId,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal sourceBalanceBefore,
        BigDecimal sourceBalanceAfter,
        BigDecimal targetBalanceBefore,
        BigDecimal targetBalanceAfter,
        String location,
        String deviceId,
        String referenceNote,
        LocalDateTime transactionDate
) {}