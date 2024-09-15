package org.example.account;

import org.example.transaction.TransactionContext;
import java.math.BigDecimal;

public class Account {
    private final String id;
    private BigDecimal balance;

    public Account(String id, BigDecimal initialBalance) {
        this.id = id;
        this.balance = initialBalance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getId() {
        return id;
    }

    public boolean provisionalUpdate(BigDecimal delta, TransactionContext context) {
        BigDecimal currentBalance = context.getProvisionalBalance(this);
        if (currentBalance == null) {
            context.setOriginalBalance(this, this.balance);
            currentBalance = this.balance;
        }
        BigDecimal newBalance = currentBalance.add(delta);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        context.setProvisionalBalance(this, newBalance);
        return true;
    }

    public void commitProvisionalBalance(TransactionContext context) {
        BigDecimal provisionalBalance = context.getProvisionalBalance(this);
        if (provisionalBalance != null) {
            this.balance = provisionalBalance;
            context.removeProvisionalBalance(this);
        }
    }

    public void rollbackToOriginalBalance(TransactionContext context) {
        BigDecimal originalBalance = context.getOriginalBalance(this);
        if (originalBalance != null) {
            this.balance = originalBalance;
            context.removeProvisionalBalance(this);
        }
    }
}
