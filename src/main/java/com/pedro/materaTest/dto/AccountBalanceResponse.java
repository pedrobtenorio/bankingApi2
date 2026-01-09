package com.pedro.materaTest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AccountBalanceResponse {

    private UUID accountId;
    private BigDecimal balance;
}
