package org.example.transaction;

import org.example.account.Account;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionLog {
    private final ConcurrentMap<Integer, TransactionRecord> log = new ConcurrentHashMap<>();
    private final AtomicInteger transactionIdGenerator = new AtomicInteger(0);

    public int startTransaction() {
        int transactionId = transactionIdGenerator.incrementAndGet();
        log.put(transactionId, new TransactionRecord(transactionId));
        return transactionId;
    }

    public void commitTransaction(int transactionId) {
        TransactionRecord record = log.get(transactionId);
        if (record != null) {
            record.setCommitted(true);
        }
    }

    public void rollbackTransaction(int transactionId) {
        TransactionRecord record = log.get(transactionId);
        if (record != null) {
            record.setRolledBack(true);
            for (TransactionEntry entry : record.getEntries()) {
                Account account = entry.account();
                account.rollback(entry.amount());
            }
        }
    }

    public void logTransactionEntry(int transactionId, Account account, BigDecimal amount) {
        TransactionRecord record = log.get(transactionId);
        if (record != null) {
            record.addEntry(new TransactionEntry(account, amount));
        }
    }
}
