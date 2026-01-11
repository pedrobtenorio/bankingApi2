package com.pedro.materaTest.dto;

import com.pedro.materaTest.domain.enums.EntryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryRequest {

    @NotNull
    private EntryType type;

    @NotNull
    @Positive
    private BigDecimal amount;

    @Size(max = 255)
    private String description;
}
