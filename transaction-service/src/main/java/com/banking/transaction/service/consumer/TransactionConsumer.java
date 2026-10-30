package com.banking.transaction.service.consumer;

import com.banking.transaction.dto.TransactionEventDto;
import com.banking.transaction.exception.TransactionException;
import com.banking.transaction.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionConsumer.class);
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    public TransactionConsumer(TransactionService transactionService, ObjectMapper objectMapper) {
        this.transactionService = transactionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "bank.raw-transactions",
            groupId = "banking-group",
            autoStartup = "true",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionEvent(String payload) {
        try {
            // Manually convert the JSON string from Kafka into your DTO
            TransactionEventDto event = objectMapper.readValue(payload, TransactionEventDto.class);

            LOGGER.info("====> Processing Transaction Event UUID: {}, Type: {}, Amount: {}",
                    event.transactionUuid(), event.transactionType(), event.amount());

            transactionService.processTransactionEvent(event);

        }catch (JsonProcessingException e) {
            throw new TransactionException("Failed to deserialize transaction event.");
        }catch (Exception e) {
            LOGGER.error("Failed to process transaction event: {}", e.getMessage(), e);

//            // CREATE AND PUBLISH FAILURE EVENT TO A SEPARATE RESULT TOPIC
//            TransactionEventDto failureEvent = new TransactionEventDto(
//                    event.transactionUuid(), event.fromAccountId(), event.toAccountId(),
//                    event.amount(), event.sourceBalanceBefore(), event.sourceBalanceAfter(),
//                    event.targetBalanceBefore(), event.targetBalanceAfter(),
//                    event.transactionType(), "FAILED", e.getMessage(),
//                    event.location(), event.deviceId(), event.timestamp()
//            );
//
//            // Send to a separate response topic so account-service can consume it for rollback
//            kafkaTemplate.send("bank.transaction-results", event.transactionUuid(), failureEvent);

            //throw e; // Throw exception so ErrorHandler can route it to DLT if necessary
            throw new TransactionException("Failed to process transaction event: {}"+e.getMessage());
        }
    }
}