package com.pedro.materaTest.controller;

import com.pedro.materaTest.dto.*;
import com.pedro.materaTest.service.AccountService;
import com.pedro.materaTest.service.ApplyEntriesResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Account creation, balance, and ledger entries")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{accountId}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Apply ledger entries", description = "Apply one or more debit/credit entries to an account.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entries applied"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Account locked"),
            @ApiResponse(responseCode = "422", description = "Insufficient funds")
    })
    public BatchLedgerEntryResponse applyEntries(
            @Parameter(description = "Account id", required = true)
            @PathVariable UUID accountId,
            @Valid @RequestBody BatchLedgerEntryRequest request) {
        ApplyEntriesResult result = accountService.applyEntries(accountId, request.getEntries());
        var responses = result.entries().stream()
                .map(entry -> new LedgerEntryResponse(
                        entry.getId(),
                        entry.getType(),
                        entry.getAmount(),
                        entry.getDescription(),
                        entry.getOccurredAt()))
                .collect(Collectors.toList());
        return new BatchLedgerEntryResponse(accountId, result.balance(), responses);
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Return the current balance for an account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance returned"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public AccountBalanceResponse getBalance(@PathVariable UUID accountId) {
        BigDecimal balance = accountService.getBalance(accountId);
        return new AccountBalanceResponse(accountId, balance);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create account", description = "Create a new account with an optional initial balance.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public CreateAccountResponse createAccount(@Valid @RequestBody(required = false) CreateAccountRequest request) {
        BigDecimal initialBalance = request == null ? null : request.getInitialBalance();
        var account = accountService.createAccount(initialBalance);
        return new CreateAccountResponse(account.getId(), account.getBalance());
    }
}
