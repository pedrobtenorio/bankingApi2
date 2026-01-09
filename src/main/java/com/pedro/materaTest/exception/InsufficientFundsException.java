package com.pedro.materaTest.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(BigDecimal balance, BigDecimal amount) {
        super("Insufficient funds. Balance: " + balance + ", amount: " + amount);
    }
}
