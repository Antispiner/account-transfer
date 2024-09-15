package org.example.transaction;

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
        TransactionRecord record = log.remove(transactionId);
        if (record != null) {
            record.setCommitted(true);
        }
    }

    public void rollbackTransaction(int transactionId) {
        TransactionRecord record = log.remove(transactionId);
        if (record != null) {
            record.setRolledBack(true);
        }
    }
}
