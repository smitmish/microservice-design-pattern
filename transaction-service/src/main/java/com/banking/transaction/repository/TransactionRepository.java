package com.banking.transaction.repository;

import com.banking.transaction.entity.Transaction;
import com.banking.transaction.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    // Fetch complete transaction statement for an account ordered by newest first
    List<Transaction> findBySourceAccountIdOrTargetAccountIdOrderByTransactionDateDesc(
            Long sourceAccountId,
            Long targetAccountId
    );

    // Enforce Kafka event idempotency check
    boolean existsByTransactionUuid(String transactionUuid);

    // Retrieve by Idempotency UUID
    Optional findByTransactionUuid(String transactionUuid);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.sourceAccountId = :accountNumber " +
            "AND t.status = 'COMPLETED' AND t.transactionType IN ('DEBIT', 'TRANSFER') " +
            "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate")
    BigDecimal sumAmountByAccountAndDate(
            @Param("accountNumber") Long accountNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}