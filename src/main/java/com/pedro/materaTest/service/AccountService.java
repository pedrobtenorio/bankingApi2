package com.pedro.materaTest.service;

import com.pedro.materaTest.domain.entity.Account;
import com.pedro.materaTest.domain.entity.LedgerEntry;
import com.pedro.materaTest.domain.enums.EntryType;
import com.pedro.materaTest.dto.LedgerEntryRequest;
import com.pedro.materaTest.exception.AccountNotFoundException;
import com.pedro.materaTest.exception.InsufficientFundsException;
import com.pedro.materaTest.exception.InvalidEntryException;
import com.pedro.materaTest.repository.AccountRepository;
import com.pedro.materaTest.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public AccountService(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public ApplyEntriesResult applyEntries(UUID accountId, List<LedgerEntryRequest> requests) {
        validateRequests(requests);
        Account account = loadAccountForUpdate(accountId);

        BigDecimal balance = account.getBalance();
        List<LedgerEntry> entries = new ArrayList<>(requests.size());

        for (LedgerEntryRequest request : requests) {
            validateRequest(request);
            balance = applyRequest(account, request, balance, entries);
        }

        account.setBalance(balance);
        persistEntries(entries);
        return new ApplyEntriesResult(balance, entries);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return account.getBalance();
    }

    @Transactional
    public Account createAccount(BigDecimal initialBalance) {
        if (initialBalance != null) {
            if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidEntryException("Initial balance must be zero or positive");
            }
        }
        Account account = Account.create(initialBalance);
        return accountRepository.save(account);
    }

    private void validateRequests(List<LedgerEntryRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new InvalidEntryException("Entries list must not be empty");
        }
    }

    private Account loadAccountForUpdate(UUID accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void validateRequest(LedgerEntryRequest request) {
        if (request == null) {
            throw new InvalidEntryException("Entry must not be null");
        }
    }

    private BigDecimal applyRequest(Account account,
                                    LedgerEntryRequest request,
                                    BigDecimal balance,
                                    List<LedgerEntry> entries) {
        BigDecimal newBalance = applyBalanceChange(request, balance);
        entries.add(createLedgerEntry(account, request));
        return newBalance;
    }

    private BigDecimal applyBalanceChange(LedgerEntryRequest request, BigDecimal balance) {
        BigDecimal amount = request.getAmount();
        if (request.getType() == EntryType.DEBIT) {
            ensureSufficientFunds(balance, amount);
            return balance.subtract(amount);
        }
        return balance.add(amount);
    }

    private void ensureSufficientFunds(BigDecimal balance, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(balance, amount);
        }
    }

    private LedgerEntry createLedgerEntry(Account account, LedgerEntryRequest request) {
        return LedgerEntry.create(account, request.getType(), request.getAmount(), request.getDescription());
    }

    private void persistEntries(List<LedgerEntry> entries) {
        ledgerEntryRepository.saveAll(entries);
    }

}
