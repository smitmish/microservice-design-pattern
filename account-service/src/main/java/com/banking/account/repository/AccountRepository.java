package com.banking.account.repository;

import com.banking.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    // Find account by account number (e.g., 11xxxxxxxx or 12xxxxxxxx)
    Optional<Account> findByAccountNumber(Long accountNumber);

    // Find all accounts belonging to a specific customer ID
    List<Account> findByCustomerId(Long customerId);

    // Check if an account number already exists
    boolean existsByAccountNumber(Long accountNumber);

    // Find account by business account number (e.g., 11xxxxxxxx or 12xxxxxxxx)
    Optional<Account> findByAccountId(Long accountId);
}