package org.example.transaction;

import org.example.account.Account;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class TransactionContext {
    private final int transactionId;
    private final Map<Account, BigDecimal> provisionalBalances = new HashMap<>();
    private final Map<Account, BigDecimal> originalBalances = new HashMap<>();

    public TransactionContext(int transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getProvisionalBalance(Account account) {
        return provisionalBalances.get(account);
    }

    public void setProvisionalBalance(Account account, BigDecimal balance) {
        provisionalBalances.put(account, balance);
    }

    public void removeProvisionalBalance(Account account) {
        provisionalBalances.remove(account);
        originalBalances.remove(account);
    }

    public void setOriginalBalance(Account account, BigDecimal balance) {
        if (!originalBalances.containsKey(account)) {
            originalBalances.put(account, balance);
        }
    }

    public BigDecimal getOriginalBalance(Account account) {
        return originalBalances.get(account);
    }

    public Map<Account, BigDecimal> getAllProvisionalBalances() {
        return provisionalBalances;
    }
}
