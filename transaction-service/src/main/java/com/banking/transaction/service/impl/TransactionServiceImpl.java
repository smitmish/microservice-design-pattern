package com.banking.transaction.service.impl;

import com.banking.transaction.dto.TransactionEventDto;
import com.banking.transaction.dto.TransactionResponseDto;
import com.banking.transaction.entity.Transaction;
import com.banking.transaction.enums.TransactionStatus;
import com.banking.transaction.enums.TransactionType;
import com.banking.transaction.exception.TransactionException;
import com.banking.transaction.mapper.TransactionMapper;
import com.banking.transaction.repository.TransactionRepository;
import com.banking.transaction.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.TransactionalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void processTransactionEvent(TransactionEventDto eventDto) {
        Transaction tx = null;
        // 1. Idempotency Check using Kafka Event UUID
        if (transactionRepository.existsByTransactionUuid(eventDto.transactionUuid())) {
            LOGGER.warn("Duplicate transaction event ignored. UUID: {}", eventDto.transactionUuid());
            return;
        }

        // 2. Map Epoch Millis to LocalDateTime
        LocalDateTime txTime = (eventDto.timestamp() != null)
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(eventDto.timestamp()), ZoneId.systemDefault())
                : LocalDateTime.now();

        //3. In case of Transaction type as Transfer/Debit. Calculate user's total transactions for today.
        if(eventDto.transactionType().equalsIgnoreCase(String.valueOf(TransactionType.TRANSFER)) ||
                eventDto.transactionType().equalsIgnoreCase(String.valueOf(TransactionType.DEBIT))){
            LocalDateTime startOfDay = txTime.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = txTime.toLocalDate().plusDays(1).atStartOfDay();

            BigDecimal totalToday = transactionRepository.sumAmountByAccountAndDate(
                    eventDto.fromAccountId(), startOfDay, endOfDay
            );
            if (totalToday == null) {
                totalToday = BigDecimal.ZERO;
            }

            // 4. Business Rule: Enforce a Daily Limit of $10,000
            BigDecimal dailyLimit = new BigDecimal("10000.00");
            if (totalToday.add(eventDto.amount()).compareTo(dailyLimit) > 0) {
                //Prepare the successful transaction.
                tx = Transaction.builder()
                        .transactionUuid(eventDto.transactionUuid())
                        .sourceAccountId(eventDto.fromAccountId())
                        .targetAccountId(eventDto.toAccountId())
                        .transactionType(TransactionType.valueOf(eventDto.transactionType()))
                        .amount(eventDto.amount())
                        .sourceBalanceBefore(eventDto.sourceBalanceBefore())
                        .sourceBalanceAfter(eventDto.sourceBalanceAfter())
                        .targetBalanceBefore(eventDto.targetBalanceBefore())
                        .targetBalanceAfter(eventDto.targetBalanceAfter())
                        .location(eventDto.location())
                        .deviceId(eventDto.deviceId())
                        .referenceNote(eventDto.referenceNote())
                        .transactionDate(txTime)
                        .status(TransactionStatus.REJECTED)
                        .build();

                // Publish REJECTED event back to Kafka for SAGA Compensation
                TransactionEventDto rejectedEvent = new TransactionEventDto(
                        eventDto.transactionUuid(), eventDto.fromAccountId(), eventDto.toAccountId(),
                        eventDto.amount(), eventDto.sourceBalanceBefore(), eventDto.sourceBalanceAfter(),
                        eventDto.targetBalanceBefore(), eventDto.targetBalanceAfter(),
                        eventDto.transactionType(), TransactionStatus.REJECTED.toString(), "Business Error: Daily transaction limit of $10,000 exceeded.",
                        eventDto.location(), eventDto.deviceId(), eventDto.timestamp()
                );

                kafkaTemplate.send("bank.transaction-results", String.valueOf(eventDto.transactionUuid()), prepareKafkaString(rejectedEvent));
                LOGGER.info("Business Error: Daily transaction limit of $10,000 exceeded.");
            }else{
                // 5. Prepare the successful transaction.
                tx = Transaction.builder()
                        .transactionUuid(eventDto.transactionUuid())
                        .sourceAccountId(eventDto.fromAccountId())
                        .targetAccountId(eventDto.toAccountId())
                        .transactionType(TransactionType.valueOf(eventDto.transactionType()))
                        .amount(eventDto.amount())
                        .sourceBalanceBefore(eventDto.sourceBalanceBefore())
                        .sourceBalanceAfter(eventDto.sourceBalanceAfter())
                        .targetBalanceBefore(eventDto.targetBalanceBefore())
                        .targetBalanceAfter(eventDto.targetBalanceAfter())
                        .location(eventDto.location())
                        .deviceId(eventDto.deviceId())
                        .referenceNote(eventDto.referenceNote())
                        .transactionDate(txTime)
                        .status(TransactionStatus.COMPLETED)
                        .build();
            }
        }
        else{
            // 5. Prepare the successful transaction.
            tx = Transaction.builder()
                    .transactionUuid(eventDto.transactionUuid())
                    .sourceAccountId(eventDto.fromAccountId())
                    .targetAccountId(eventDto.toAccountId())
                    .transactionType(TransactionType.valueOf(eventDto.transactionType()))
                    .amount(eventDto.amount())
                    .sourceBalanceBefore(eventDto.sourceBalanceBefore())
                    .sourceBalanceAfter(eventDto.sourceBalanceAfter())
                    .targetBalanceBefore(eventDto.targetBalanceBefore())
                    .targetBalanceAfter(eventDto.targetBalanceAfter())
                    .location(eventDto.location())
                    .deviceId(eventDto.deviceId())
                    .referenceNote(eventDto.referenceNote())
                    .transactionDate(txTime)
                    .status(TransactionStatus.COMPLETED)
                    .build();
        }
        transactionRepository.save(tx);
        LOGGER.info("Successfully persisted transaction record. Tx ID: {}", tx.getTransactionId());
    }

    @Override
    @Transactional(readOnly = true)
    public List getAccountTransactions(Long accountId) {
        return transactionRepository
                .findBySourceAccountIdOrTargetAccountIdOrderByTransactionDateDesc(accountId, accountId)
                .stream()
                .map(TransactionMapper::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private String prepareKafkaString(TransactionEventDto rejectedEvent) {
        try {
            return objectMapper.writeValueAsString(rejectedEvent);
        } catch (JsonProcessingException e) {
            throw new TransactionException("Failed to serialize transaction event.");
        }
    }
}