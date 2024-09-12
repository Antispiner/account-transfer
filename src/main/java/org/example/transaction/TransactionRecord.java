package org.example.transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionRecord {
    private final int id;
    private final List<TransactionEntry> entries = new ArrayList<>();
    private boolean committed = false;
    private boolean rolledBack = false;

    public TransactionRecord(int id) {
        this.id = id;
    }

    public void addEntry(TransactionEntry entry) {
        entries.add(entry);
    }

    public List<TransactionEntry> getEntries() {
        return entries;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    public boolean isRolledBack() {
        return rolledBack;
    }

    public void setRolledBack(boolean rolledBack) {
        this.rolledBack = rolledBack;
    }
}
