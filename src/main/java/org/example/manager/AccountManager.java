package org.example.manager;

import org.example.account.Account;
import org.example.account.AccountSegment;
import org.example.error.ErrorHandler;
import org.example.transaction.TransactionContext;
import org.example.transaction.TransactionLog;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccountManager {
    private final List<AccountSegment> segments;
    private final TransactionLog transactionLog = new TransactionLog();
    private final ErrorHandler errorHandler;
    private final ConcurrentMap<String, Boolean> idempotencyKeys = new ConcurrentHashMap<>();

    public AccountManager(int numberOfSegments, List<Account> initialAccounts, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        segments = new ArrayList<>(numberOfSegments);
        for (int i = 0; i < numberOfSegments; i++) {
            segments.add(new AccountSegment());
        }
        for (Account account : initialAccounts) {
            getSegmentByAccountId(account.getId()).addAccount(account);
        }
    }

    public AccountSegment getSegmentByAccountId(String accountId) {
        int segmentIndex = Math.abs(accountId.hashCode()) % segments.size();
        return segments.get(segmentIndex);
    }

    public Account getAccount(String accountId) {
        AccountSegment segment = getSegmentByAccountId(accountId);
        return segment.getAccount(accountId);
    }

    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        for (AccountSegment segment : segments) {
            accounts.addAll(segment.getAllAccounts());
        }
        return accounts;
    }

    public boolean transferMoney(String idempotencyKey, String fromAccountId, String toAccountId, BigDecimal amount) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            errorHandler.logError("Idempotency-Key header is missing or empty",
                    new IllegalArgumentException("Invalid Idempotency-Key"));
            return false;
        }

        if (idempotencyKeys.putIfAbsent(idempotencyKey, true) != null) {
            errorHandler.logError("Duplicate transaction detected for idempotency key: " + idempotencyKey,
                    new IllegalStateException("Duplicate transaction"));
            return false;
        }

        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);

        if (fromAccount == null || toAccount == null) {
            errorHandler.logError("Account not found",
                    new IllegalArgumentException("Invalid account ID(s)"));
            return false;
        }

        int transactionId = transactionLog.startTransaction();
        TransactionContext context = new TransactionContext(transactionId);

        List<Account> accountsToLock = Arrays.asList(fromAccount, toAccount);
        accountsToLock.sort(Comparator.comparing(Account::getId));

        AtomicBoolean success = new AtomicBoolean(false);
        try {
            synchronizedAccounts(accountsToLock, () -> {
                try {
                    if (!fromAccount.provisionalUpdate(amount.negate(), context) ||
                            !toAccount.provisionalUpdate(amount, context)) {
                        throw new RuntimeException("Transaction validation failed");
                    }

                    fromAccount.commitProvisionalBalance(context);
                    toAccount.commitProvisionalBalance(context);

                    transactionLog.commitTransaction(transactionId);
                    success.set(true);
                } finally {
                    if (!success.get()) {
                        rollbackTransaction(context);
                    }
                }
            });
            return success.get();
        } catch (Exception e) {
            errorHandler.logError("Error processing transaction", e);
            transactionLog.rollbackTransaction(transactionId);
            return false;
        }
    }


    private void rollbackTransaction(TransactionContext context) {
        for (Account account : context.getAllProvisionalBalances().keySet()) {
            account.rollbackToOriginalBalance(context);
        }
    }

    private void synchronizedAccounts(List<Account> accounts, Runnable action) {
        accounts.forEach(account -> Objects.requireNonNull(account, "Account cannot be null"));
        synchronizedAccountsRecursive(accounts, 0, action);
    }

    private void synchronizedAccountsRecursive(List<Account> accounts, int index, Runnable action) {
        if (index >= accounts.size()) {
            action.run();
            return;
        }
        synchronized (accounts.get(index)) {
            synchronizedAccountsRecursive(accounts, index + 1, action);
        }
    }

}
