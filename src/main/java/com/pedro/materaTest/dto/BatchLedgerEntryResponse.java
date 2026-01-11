package com.pedro.materaTest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BatchLedgerEntryResponse {

    private UUID accountId;

    private BigDecimal balance;

    private List<LedgerEntryResponse> entries;
}
