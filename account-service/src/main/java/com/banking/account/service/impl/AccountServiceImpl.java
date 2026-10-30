package com.banking.account.service.impl;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.TransactionEventDto;
import com.banking.account.dto.TransferFundDto;
import com.banking.account.entity.Account;
import com.banking.account.entity.BusinessAccount;
import com.banking.account.entity.OutboxMessage;
import com.banking.account.entity.SavingsAccount;
import com.banking.account.enums.TransactionStatus;
import com.banking.account.enums.TransactionType;
import com.banking.account.exception.AccountException;
import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.mapper.AccountMapper;
import com.banking.account.repository.AccountRepository;
import com.banking.account.repository.OutboxRepository;
import com.banking.account.service.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "banking-transactions";
    @PersistenceContext
    private EntityManager entityManager;

    public AccountServiceImpl(AccountRepository accountRepository,
                              OutboxRepository outboxRepository,
                              ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        try {
            Account account = AccountMapper.mapToAccount(accountDto);
            // Fetch sequence values for secondary PKs & account numbers based on type
            // JDK 21 Pattern Matching Switch inside Service
            switch (account) {
                case SavingsAccount savings -> {
                    savings.setSavingsAccountId(getNextSequenceValue("savings_account_id_seq"));
                    savings.setAccountNumber(getNextSequenceValue("saving_account_num_seq"));
                }
                case BusinessAccount business -> {
                    business.setBusinessAccountId(getNextSequenceValue("business_account_id_seq"));
                    business.setAccountNumber(getNextSequenceValue("business_account_num_seq"));
                }
                case null, default -> throw new AccountException("Invalid or unsupported account type");
            }
            Account savedAccount = accountRepository.save(account);
            // Hibernate handles account_id via account_id_seq and
            // propagates to child table via @PrimaryKeyJoinColumn
            return AccountMapper.mapToAccountDto(savedAccount);
        } catch(AccountException e) {
            throw e;
        } catch (Exception e) {
            throw new AccountException("Exception occurs in createAccount: " + e.getMessage());
        }
    }

    private Long getNextSequenceValue(String sequenceName) {
        Number result = (Number) entityManager
                .createNativeQuery("SELECT " + sequenceName + ".NEXTVAL FROM DUAL")
                .getSingleResult();
        return result.longValue();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountByAccountNumber(Long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account with account number " +
                        "%d does not exist: " + accountNumber));
        return AccountMapper.mapToAccountDto(account);
    }

    @Override
    @Transactional
    public AccountDto deposit(Long accountNumber, BigDecimal amount, String location, String deviceId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account with account number " +
                        "%d does not exist: " + accountNumber));

        BigDecimal balanceBefore = account.getBalance();
        account.deposit(amount);
        BigDecimal balanceAfter = account.getBalance();
        accountRepository.save(account);

        //Orchestrate Outbox Event: Prepare event for Kafka messaging to store records in transaction-service.
        TransactionEventDto event = new TransactionEventDto(
                java.util.UUID.randomUUID().toString(),
    null,
                account.getAccountId(),
                amount,
                null,
                null,
                balanceBefore,
                balanceAfter,
                String.valueOf(TransactionType.CREDIT),
                String.valueOf(TransactionStatus.SUCCESS),
                "Deposit to account",
                location,
                deviceId,
                System.currentTimeMillis()
        );
        saveOutboxMessage("bank.raw-transactions", event);
        return AccountMapper.mapToAccountDto(account);
    }

    @Override
    @Transactional
    public AccountDto withdrawal(Long accountNumber, BigDecimal amount, String location, String deviceId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account with account number " +
                        "%d does not exist: " + accountNumber));

        BigDecimal balanceBefore = account.getBalance();
        // Polymorphism handles whether this is Savings or Business logic automatically!
        account.withdraw(amount);
        BigDecimal balanceAfter = account.getBalance();
        accountRepository.save(account);

        TransactionEventDto event = new TransactionEventDto(
                java.util.UUID.randomUUID().toString(),
                account.getAccountId(),
                null,
                amount,
                balanceBefore,
                balanceAfter,
                null,null,
                String.valueOf(TransactionType.DEBIT),
                String.valueOf(TransactionStatus.SUCCESS),
                null,
                location,
                deviceId,
                System.currentTimeMillis()
        );
        saveOutboxMessage("bank.raw-transactions", event);
        return AccountMapper.mapToAccountDto(account);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountMapper::mapToAccountDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAccount(Long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account does not exist: " + accountNumber));
        accountRepository.delete(account);
    }

    @Override
    @Transactional
    public AccountDto transferFunds(TransferFundDto transferFundDto) {
        // 1. Fetch Source and Destination Accounts
        Account fromAccount = accountRepository.findByAccountNumber(transferFundDto.fromAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Source account with account number "
                        + transferFundDto.fromAccountNumber() + " does not exist"));

        Account toAccount = accountRepository.findByAccountNumber(transferFundDto.toAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Destination account with account number "
                        + transferFundDto.toAccountNumber() + " does not exist"));

        // Capture initial balances for audit/outbox event
        BigDecimal sourceBalanceBefore = fromAccount.getBalance();
        BigDecimal targetBalanceBefore = toAccount.getBalance();

        // 2. Perform Debit on Source (Polymorphically triggers Savings or Business check)
        fromAccount.withdraw(transferFundDto.amount());

        // 3. Perform Credit on Destination
        toAccount.deposit(transferFundDto.amount());

        BigDecimal sourceBalanceAfter = fromAccount.getBalance();
        BigDecimal targetBalanceAfter = toAccount.getBalance();

        // 4. Save both entity updates
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 5. Construct Single Transaction Outbox Event
        TransactionEventDto event = new TransactionEventDto(
                java.util.UUID.randomUUID().toString(),
                fromAccount.getAccountId(),
                toAccount.getAccountId(),
                transferFundDto.amount(),
                sourceBalanceBefore,
                sourceBalanceAfter,
                targetBalanceBefore,
                targetBalanceAfter,
                String.valueOf(TransactionType.TRANSFER),
                String.valueOf(TransactionStatus.SUCCESS),
                transferFundDto.referenceNote() != null ? transferFundDto.referenceNote() : "Fund transfer",
                transferFundDto.location(),
                transferFundDto.deviceId(),
                System.currentTimeMillis()
        );

        // 6. Stage Outbox Record via Helper
        saveOutboxMessage("bank.raw-transactions", event);

        return AccountMapper.mapToAccountDto(fromAccount);
    }

    private void saveOutboxMessage(String topic, TransactionEventDto event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxMessage(topic, payload));
        } catch (JsonProcessingException e) {
            throw new AccountException("Failed to serialize transaction outbox event payload", e);
        }
    }

    @Transactional
    public void rollbackTransfer(TransactionEventDto event) {
        if(event.transactionType().equalsIgnoreCase(String.valueOf(TransactionType.TRANSFER))){
            Account toAccount = accountRepository.findByAccountId(event.toAccountId())
                    .orElseThrow(() -> new AccountNotFoundException("To Account is not valid"));
            //Withdrawl it from credit account
            toAccount.withdraw(event.amount());
        }
        Account fromAccount = accountRepository.findByAccountId(event.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("From Account is not valid"));
        // Credit back to debited amount
        fromAccount.deposit(event.amount());
        accountRepository.save(fromAccount);
        System.out.println("SAGA COMPENSATION ROLLBACK: Refunded " + event.amount() +
                " to account " + event.fromAccountId() + " | Reason: " + event.referenceNote());
    }
}