package com.banking.transaction.mapper;

import com.banking.transaction.dto.TransactionResponseDto;
import com.banking.transaction.entity.Transaction;

public class TransactionMapper {
    public static TransactionResponseDto mapToResponseDto(Transaction t) {
        return new TransactionResponseDto(
                t.getTransactionId(),
                t.getTransactionUuid(),
                t.getSourceAccountId(),
                t.getTargetAccountId(),
                t.getTransactionType(),
                t.getAmount(),
                t.getSourceBalanceBefore(),
                t.getSourceBalanceAfter(),
                t.getTargetBalanceBefore(),
                t.getTargetBalanceAfter(),
                t.getLocation(),
                t.getDeviceId(),
                t.getReferenceNote(),
                t.getTransactionDate()
        );
    }
}
