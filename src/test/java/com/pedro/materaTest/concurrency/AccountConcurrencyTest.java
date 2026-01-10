package com.pedro.materaTest.concurrency;

import com.pedro.materaTest.domain.enums.EntryType;
import com.pedro.materaTest.dto.LedgerEntryRequest;
import com.pedro.materaTest.exception.InsufficientFundsException;
import com.pedro.materaTest.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AccountConcurrencyTest {

    @Autowired
    private AccountService accountService;

    @Test
    void concurrentDebits_areSerializedByPessimisticLock() throws InterruptedException {
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal debitAmount = new BigDecimal("15.00");
        int attempts = 10;

        UUID accountId = accountService.createAccount(initialBalance).getId();

        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(attempts);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < attempts; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    accountService.applyEntries(accountId, List.of(
                            new LedgerEntryRequest(EntryType.DEBIT, debitAmount, "debit")));
                    successCount.incrementAndGet();
                } catch (InsufficientFundsException ex) {
                    failureCount.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        int expectedSuccesses = initialBalance.divideToIntegralValue(debitAmount).intValueExact();
        assertEquals(expectedSuccesses, successCount.get());
        assertEquals(attempts - expectedSuccesses, failureCount.get());

        BigDecimal expectedBalance = initialBalance.subtract(debitAmount.multiply(new BigDecimal(expectedSuccesses)));
        BigDecimal actualBalance = accountService.getBalance(accountId);
        assertEquals(0, expectedBalance.compareTo(actualBalance));
    }
}
