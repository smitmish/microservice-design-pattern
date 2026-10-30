package com.banking.account.entity;

import com.banking.account.exception.AccountException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING, length = 15)
@Getter @Setter
@NoArgsConstructor
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acc_seq_gen")
    @SequenceGenerator(name = "acc_seq_gen", sequenceName = "account_id_seq", allocationSize = 1)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number", nullable = false, unique = true)
    private Long accountNumber;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //Encapsulate Debit Business Rule
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException("Deposit amount must be greater than zero.");
        }
        this.balance = this.balance.add(amount);
    }

    // Abstract method to enforce polymorphic withdrawal validation
    public abstract void withdraw(BigDecimal amount);

}