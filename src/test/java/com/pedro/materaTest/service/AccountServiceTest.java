package com.pedro.materaTest.service;

import com.pedro.materaTest.domain.entity.Account;
import com.pedro.materaTest.domain.entity.LedgerEntry;
import com.pedro.materaTest.domain.enums.EntryType;
import com.pedro.materaTest.dto.LedgerEntryRequest;
import com.pedro.materaTest.exception.InsufficientFundsException;
import com.pedro.materaTest.repository.AccountRepository;
import com.pedro.materaTest.repository.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void applyEntries_updatesBalanceAndPersistsEntries() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.create(new BigDecimal("100.00"));
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(ledgerEntryRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var requests = List.of(
                new LedgerEntryRequest(EntryType.DEBIT, new BigDecimal("20.00"), "debit"),
                new LedgerEntryRequest(EntryType.CREDIT, new BigDecimal("10.00"), "credit"));

        ApplyEntriesResult result = accountService.applyEntries(accountId, requests);

        assertBigDecimal("90.00", result.balance());
        assertEquals(2, result.entries().size());
        assertBigDecimal("90.00", account.getBalance());
        verify(accountRepository).findByIdForUpdate(accountId);
        ArgumentCaptor<Iterable<LedgerEntry>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(ledgerEntryRepository).saveAll(captor.capture());
                assertEquals(2, ((List<?>) captor.getValue()).size());
    }

    @Test
    void applyEntries_throwsWhenInsufficientFunds() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.create(new BigDecimal("10.00"));
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        var requests = List.of(new LedgerEntryRequest(EntryType.DEBIT, new BigDecimal("20.00"), "debit"));

        assertThrows(InsufficientFundsException.class, () -> accountService.applyEntries(accountId, requests));
        verifyNoInteractions(ledgerEntryRepository);
    }

    @Test
    void createAccount_withInitialBalancePersists() {
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var account = accountService.createAccount(new BigDecimal("55.50"));

        assertBigDecimal("55.50", account.getBalance());
        verify(accountRepository).save(any(Account.class));
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
