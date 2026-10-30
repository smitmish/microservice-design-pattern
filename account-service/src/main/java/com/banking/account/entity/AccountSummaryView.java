package com.banking.account.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "account_summary_view")
@Getter
@Setter
@NoArgsConstructor
public class AccountSummaryView {
    @Id
    private Long accountNumber;

    private String accountHolderName;
    private BigDecimal currentBalance;
    private String status;
    // Discriminator / Type field to distinguish sub-types
    private String accountType; // "SAVINGS" or "BUSINESS"
    // Savings-specific fields (null if it's a Business account)
    private BigDecimal interestRate;
    // Business-specific fields (null if it's a Savings account)
    private String taxId;
    private BigDecimal overdraftLimit;
}
