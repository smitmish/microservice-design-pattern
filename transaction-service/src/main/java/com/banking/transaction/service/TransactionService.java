package com.banking.transaction.service;

import com.banking.transaction.dto.TransactionEventDto;

import java.util.List;

public interface TransactionService {
    // Process & store incoming Kafka Outbox event with idempotency check
    void processTransactionEvent(TransactionEventDto eventDto);
    // Retrieve full statement history for a given account (source OR target)
    List getAccountTransactions(Long accountId);
}