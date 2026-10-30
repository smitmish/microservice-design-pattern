package com.banking.transaction.controller;

import com.banking.transaction.dto.TransactionResponseDto;
import com.banking.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{accountId}/getHistory")
    public ResponseEntity<List<TransactionResponseDto>> getHistory(@PathVariable Long accountId) {
        List<TransactionResponseDto> history = transactionService.getAccountTransactions(accountId);
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId));
    }
}