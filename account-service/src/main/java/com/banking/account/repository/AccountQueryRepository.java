package com.banking.account.repository;

import com.banking.account.entity.AccountSummaryView;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountQueryRepository extends JpaRepository<AccountSummaryView, Long> {
    @Transactional(readOnly = true)
    Optional<AccountSummaryView> findByAccountNumber(Long accountNumber);

}
