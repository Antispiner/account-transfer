package org.example.account;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class AccountSegment {
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    public void addAccount(Account account) {
        accounts.put(account.getId(), account);
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }
}
