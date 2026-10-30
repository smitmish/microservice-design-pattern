package com.banking.account.controller;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.TransferFundDto;
import com.banking.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/addAccount")
    public ResponseEntity<AccountDto> addAccount(@RequestBody AccountDto accountDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(accountDto));
    }

    @GetMapping("/{id}/getAccountById")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountByAccountNumber(id));
    }

    @PutMapping("/{id}/deposit")
    public ResponseEntity deposit(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "X-Location", defaultValue = "UNKNOWN_LOCATION") String location,
            @RequestHeader(value = "X-Device-Id", defaultValue = "UNKNOWN_DEVICE") String deviceId) {

        AccountDto updatedAccount = accountService.deposit(id, amount, location, deviceId);
        return ResponseEntity.ok(updatedAccount);
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity withdrawal(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "X-Location", defaultValue = "UNKNOWN_LOCATION") String location,
            @RequestHeader(value = "X-Device-Id", defaultValue = "UNKNOWN_DEVICE") String deviceId) {

        AccountDto updatedAccount = accountService.withdrawal(id, amount, location, deviceId);
        return ResponseEntity.ok(updatedAccount);
    }

    @GetMapping("/getAllAccounts")
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @DeleteMapping("/{id}/deleteAccount")
    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok("Account has been deleted successfully");
    }

    @PostMapping("/transferFund")
    public ResponseEntity<String> transferFund(@RequestBody TransferFundDto transferFundDto) {
        accountService.transferFunds(transferFundDto);
        return ResponseEntity.ok("Amount has been transferred successfully");
    }
}