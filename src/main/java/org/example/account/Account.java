package org.example.account;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public class Account {
    private final String id;
    private final AtomicLong balance;

    public Account(String id, BigDecimal initialBalance) {
        this.id = id;
        this.balance = new AtomicLong(initialBalance.movePointRight(2).longValue());
    }

    public BigDecimal getBalance() {
        return BigDecimal.valueOf(balance.get(), 2);
    }

    public boolean updateBalance(BigDecimal delta) {
        long deltaLong = delta.movePointRight(2).longValue();
        long currentBalance;
        long newBalance;
        do {
            currentBalance = balance.get();
            newBalance = currentBalance + deltaLong;
            if (newBalance < 0) return false;
        } while (!balance.compareAndSet(currentBalance, newBalance));
        return true;
    }

    public void rollback(BigDecimal amount) {
        updateBalance(amount.negate());
    }

    public String getId() {
        return id;
    }
}
