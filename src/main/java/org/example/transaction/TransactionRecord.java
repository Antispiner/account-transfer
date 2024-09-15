package org.example.transaction;

public class TransactionRecord {
    private final int id;
    private boolean committed = false;
    private boolean rolledBack = false;

    public TransactionRecord(int id) {
        this.id = id;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    public void setRolledBack(boolean rolledBack) {
        this.rolledBack = rolledBack;
    }
}
