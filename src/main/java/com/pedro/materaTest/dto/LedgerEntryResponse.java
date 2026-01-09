package com.pedro.materaTest.dto;

import com.pedro.materaTest.domain.enums.EntryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LedgerEntryResponse {

    private UUID id;

    private EntryType type;

    private BigDecimal amount;

    private String description;

    private Instant occurredAt;
}
