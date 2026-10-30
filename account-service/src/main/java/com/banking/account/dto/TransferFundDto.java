package com.banking.account.dto;

import java.math.BigDecimal;

public record TransferFundDto(
        Long fromAccountNumber,
        Long toAccountNumber,
        BigDecimal amount,
        String referenceNote,
        String location,
        String deviceId
) {}
