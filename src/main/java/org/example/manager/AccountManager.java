package org.example.manager;

import org.example.account.Account;
import org.example.account.AccountSegment;
import org.example.error.ErrorHandler;
import org.example.transaction.TransactionLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountManager {
    private final List<AccountSegment> segments;
    private final TransactionLog transactionLog = new TransactionLog();
    private final ErrorHandler errorHandler;
    private final Map<String, Boolean> idempotencyKeys = new HashMap<>();

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

    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        for (AccountSegment segment : segments) {
            accounts.addAll(segment.getAllAccounts());
        }
        return accounts;
    }

    public boolean transferMoney(String idempotencyKey, String fromAccountId, String toAccountId, BigDecimal amount) {
        if (idempotencyKeys.containsKey(idempotencyKey)) {
            errorHandler.logError("Duplicate transaction detected for idempotency key: " + idempotencyKey, new IllegalStateException("Duplicate transaction"));
            return false;
        }

        AccountSegment fromSegment = getSegmentByAccountId(fromAccountId);
        AccountSegment toSegment = getSegmentByAccountId(toAccountId);

        int transactionId = transactionLog.startTransaction();

        boolean success;
        try {
            synchronized (this) {
                if (fromSegment == toSegment) {
                    success = processTransfer(fromAccountId, toAccountId, amount, transactionId);
                } else {
                    synchronized (fromSegment) {
                        synchronized (toSegment) {
                            success = processTransfer(fromAccountId, toAccountId, amount, transactionId);
                        }
                    }
                }
            }

            if (success) {
                idempotencyKeys.put(idempotencyKey, true);
            } else {
                transactionLog.rollbackTransaction(transactionId);
            }
        } catch (Exception e) {
            errorHandler.logError("Error processing transaction", e);
            transactionLog.rollbackTransaction(transactionId);
            return false;
        }

        return success;
    }

    private boolean processTransfer(String fromAccountId, String toAccountId, BigDecimal amount, int transactionId) {
        Account fromAccount = getSegmentByAccountId(fromAccountId).getAccount(fromAccountId);
        Account toAccount = getSegmentByAccountId(toAccountId).getAccount(toAccountId);

        if (fromAccount == null || toAccount == null) {
            return false;
        }

        if (!fromAccount.updateBalance(amount.negate())) {
            return false;
        }

        if (!toAccount.updateBalance(amount)) {
            return false;
        }

        transactionLog.logTransactionEntry(transactionId, fromAccount, amount.negate());
        transactionLog.logTransactionEntry(transactionId, toAccount, amount);

        transactionLog.commitTransaction(transactionId);
        return true;
    }
}
