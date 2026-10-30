package com.banking.account.entity;

import com.banking.account.exception.AccountException;
import com.banking.account.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "business_account")
@DiscriminatorValue("BUSINESS")
@Getter @Setter
@NoArgsConstructor
public class BusinessAccount extends Account {

    @Column(name = "business_account_id")
    private Long businessAccountId;

    @Column(name = "tax_id", nullable = false, length = 50)
    private String taxId;

    @Column(name = "overdraft_limit", nullable = false)
    private BigDecimal overdraftLimit;

    @Override
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException("Withdrawal amount must be greater than zero.");
        }

        BigDecimal prospectiveBalance = getBalance().subtract(amount);
        BigDecimal allowedFloor = overdraftLimit.negate(); // e.g. -5000.00

        // Rule: Prospective balance cannot go below -overdraftLimit
        if (prospectiveBalance.compareTo(allowedFloor) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Overdraft limit exceeded. Allowed limit is %s (max balance %s), but prospective balance would be %s",
                            overdraftLimit, allowedFloor, prospectiveBalance)
            );
        }

        setBalance(prospectiveBalance);
    }
}