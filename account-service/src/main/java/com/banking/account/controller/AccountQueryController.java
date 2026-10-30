package com.banking.account.controller;

import com.banking.account.entity.AccountSummaryView;
import com.banking.account.service.AccountQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query/accounts")
public class AccountQueryController {

    @Autowired
    private AccountQueryService accountQueryService;

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountSummaryView> getAccountSummary(@PathVariable Long accountNumber) {
        return accountQueryService.getAccountSummary(accountNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
