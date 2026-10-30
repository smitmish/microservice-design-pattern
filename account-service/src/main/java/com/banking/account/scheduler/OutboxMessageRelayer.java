package com.banking.account.scheduler;

import com.banking.account.entity.OutboxMessage;
import com.banking.account.enums.TransactionStatus;
import com.banking.account.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
public class OutboxMessageRelayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxMessageRelayer.class);
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;

    public OutboxMessageRelayer(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate, TransactionTemplate transactionTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelay = 60000) // Checks every
    public void relayPendingMessages() {
        // Fetch top 50 pending messages to prevent OOM errors during high load
        List<OutboxMessage> pendingList = outboxRepository
                .findByStatusOrderByOutboxMsgIdAsc(String.valueOf(TransactionStatus.PENDING), PageRequest.of(0, 50));

        if (pendingList.isEmpty()) {
            return;
        }

        LOGGER.info("Found {} pending outbox messages to dispatch to Kafka...", pendingList.size());

        for (OutboxMessage message : pendingList) {
            try {
                // 1. Synchronously push payload to Kafka broker
                kafkaTemplate.send(message.getTopic(), String.valueOf(message.getOutboxMsgId()), message.getPayload()).get();
                LOGGER.info("Successfully dispatched outbox message ID: {} to topic: {}",
                        message.getOutboxMsgId(), message.getTopic());

                // 2. Programmatically commit database status update in an isolated transaction
                transactionTemplate.executeWithoutResult(status -> outboxRepository.findById(message.getOutboxMsgId()).ifPresent(msg -> {
                    msg.setStatus(String.valueOf(TransactionStatus.PROCESSED));
                    outboxRepository.saveAndFlush(msg);
                }));

            } catch (Exception e) {
                LOGGER.error("Kafka broker unreachable or send failed. Retrying in next cycle for message ID {}: {}",
                        message.getOutboxMsgId(), e.getMessage());
                break; // Break loop to maintain strict in-order sequence delivery
            }
        }
    }
}