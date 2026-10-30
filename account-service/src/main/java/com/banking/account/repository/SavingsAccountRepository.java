package com.banking.account.repository;

import com.banking.account.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount,Long> {

    Optional findByAccountNumber(Long accountNumber);
}