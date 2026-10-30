package com.banking.account.mapper;

import com.banking.account.dto.AccountDto;
import com.banking.account.entity.Account;
import com.banking.account.entity.BusinessAccount;
import com.banking.account.entity.SavingsAccount;

import java.math.BigDecimal;

public class AccountMapper {

    public static Account mapToAccount(AccountDto dto) {
        if (dto == null) return null;

        if ("SAVINGS".equalsIgnoreCase(dto.accountType())) {
            SavingsAccount savings = new SavingsAccount();
            populateCommonFields(dto, savings);
            if (dto.interestRate() != null) savings.setInterestRate(dto.interestRate());
            if (dto.minimumBalance() != null) savings.setMinimumBalance(dto.minimumBalance());
            return savings;
        } else if ("BUSINESS".equalsIgnoreCase(dto.accountType())) {
            BusinessAccount business = new BusinessAccount();
            populateCommonFields(dto, business);
            business.setTaxId(dto.taxId());
            if (dto.overdraftLimit() != null) business.setOverdraftLimit(dto.overdraftLimit());
            return business;
        }

        throw new IllegalArgumentException("Invalid account type: " + dto.accountType());
    }

    public static AccountDto mapToAccountDto(Account entity) {
        if (entity == null) return null;

        String accountType = null;
        BigDecimal interestRate = null;
        BigDecimal minimumBalance = null;
        String taxId = null;
        BigDecimal overdraftLimit = null;

        if (entity instanceof SavingsAccount savings) {
            accountType = "SAVINGS";
            interestRate = savings.getInterestRate();
            minimumBalance = savings.getMinimumBalance();
        } else if (entity instanceof BusinessAccount business) {
            accountType = "BUSINESS";
            taxId = business.getTaxId();
            overdraftLimit = business.getOverdraftLimit();
        }

        return new AccountDto(
                entity.getAccountId(),
                entity.getCustomerId(),
                entity.getAccountNumber(),
                accountType,
                entity.getBalance(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                interestRate,
                minimumBalance,
                taxId,
                overdraftLimit
        );
    }

    private static void populateCommonFields(AccountDto dto, Account entity) {
        entity.setAccountId(dto.accountId());
        entity.setCustomerId(dto.customer());
        entity.setAccountNumber(dto.accountNumber());
        entity.setBalance(dto.balance());
        entity.setCreatedBy(dto.createdBy() != null ? dto.createdBy() : "system");
    }
}