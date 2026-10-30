package com.banking.account.entity;

import com.banking.account.exception.AccountException;
import com.banking.account.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "savings_account")
@DiscriminatorValue("SAVINGS")
@PrimaryKeyJoinColumn(name = "account_id")
@Getter @Setter
@NoArgsConstructor
public class SavingsAccount extends Account {

    @Column(name = "savings_account_id")
    private Long savingsAccountId;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "minimum_balance", nullable = false)
    private BigDecimal minimumBalance;

    @Override
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException("Withdrawal amount must be greater than zero.");
        }

        BigDecimal prospectiveBalance = getBalance().subtract(amount);

        // Rule: Prospective balance cannot fall below minimum balance
        if (prospectiveBalance.compareTo(minimumBalance) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient funds. Minimum required balance is %s, but prospective balance would be %s",
                            minimumBalance, prospectiveBalance)
            );
        }

        setBalance(prospectiveBalance);
    }
}