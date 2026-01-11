package com.pedro.materaTest.service;

import com.pedro.materaTest.domain.entity.LedgerEntry;

import java.math.BigDecimal;
import java.util.List;

public record ApplyEntriesResult(BigDecimal balance, List<LedgerEntry> entries) {
}
