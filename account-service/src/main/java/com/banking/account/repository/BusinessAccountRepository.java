package com.banking.account.repository;

import com.banking.account.entity.BusinessAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessAccountRepository extends JpaRepository<BusinessAccount,Long> {

    Optional findByAccountNumber(Long accountNumber);
    Optional findByTaxId(String taxId);
}