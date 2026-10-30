package com.banking.account.service.consumer;

import com.banking.account.dto.TransactionEventDto;
import com.banking.account.exception.AccountException;
import com.banking.account.service.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SagasCompensationConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SagasCompensationConsumer.class);
    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    public SagasCompensationConsumer(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "bank.transaction-results", groupId = "banking-group")
    public void handleTransactionResult(String payload) {
        System.out.println("SAGACompensationConsumer"+payload);
        // Manually convert the JSON string from Kafka into your DTO
        TransactionEventDto event = null;
        try {
            event = objectMapper.readValue(payload, TransactionEventDto.class);
            System.out.println("event "+event);
            LOGGER.info("====> Processing Transaction Event UUID: {}, Type: {}, Amount: {}, Account ID {}",
                    event.transactionUuid(), event.transactionType(), event.amount(), event.fromAccountId());
            if ("REJECTED".equals(event.status())) {
                accountService.rollbackTransfer(event);
            }
        } catch (JsonProcessingException e) {
            throw new AccountException("Failed to deserialize payload.",e);
        }
    }
}
