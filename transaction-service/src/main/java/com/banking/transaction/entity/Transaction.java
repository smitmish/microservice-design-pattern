package com.banking.transaction.entity;

import com.banking.transaction.enums.TransactionStatus;
import com.banking.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transaction",
        indexes = {
                @Index(name = "idx_transaction_from_account", columnList = "source_account_id"),
                @Index(name = "idx_transaction_to_account", columnList = "target_account_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq_gen")
    @SequenceGenerator(
            name = "transaction_seq_gen",
            sequenceName = "transaction_id_seq",
            allocationSize = 1
    )
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private Long transactionId;

    @Column(name = "transaction_uuid", nullable = false, unique = true, length = 100)
    private String transactionUuid;

    @Column(name = "source_account_id")
    private Long sourceAccountId;

    @Column(name = "target_account_id")
    private Long targetAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "source_balance_before", precision = 15, scale = 2)
    private BigDecimal sourceBalanceBefore;

    @Column(name = "source_balance_after", precision = 15, scale = 2)
    private BigDecimal sourceBalanceAfter;

    @Column(name = "target_balance_before", precision = 15, scale = 2)
    private BigDecimal targetBalanceBefore;

    @Column(name = "target_balance_after", precision = 15, scale = 2)
    private BigDecimal targetBalanceAfter;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "reference_note", length = 255)
    private String referenceNote;

    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", length = 20)
    private TransactionStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
    }
}