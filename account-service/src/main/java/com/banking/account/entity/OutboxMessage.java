package com.banking.account.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_messages")
@DynamicInsert
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_seq_gen")
    @SequenceGenerator(
            name = "outbox_seq_gen",
            sequenceName = "outbox_msg_id_seq",
            allocationSize = 1
    )
    @Column(name = "outbox_msg_id")
    private Long outboxMsgId;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Lob // Mapped to Oracle CLOB for unlimited JSON string capacity
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "status", nullable = false, length = 10)
    @ColumnDefault("'PENDING'")
    private String status;

    @Column(name = "created_by", nullable = false, length = 50)
    @ColumnDefault("'system'")
    private String createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default Constructor (Required by JPA)
    public OutboxMessage() {}

    // Convenience Constructor for Outbox Staging
    public OutboxMessage(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
        this.status = "PENDING";
        this.createdBy = "system";
    }

    // Getters and Setters
    public Long getOutboxMsgId() {
        return outboxMsgId;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}