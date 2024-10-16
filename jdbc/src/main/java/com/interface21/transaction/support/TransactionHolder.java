package com.interface21.transaction.support;

public class TransactionHolder {

    private final Object transaction;

    private boolean mustRestoreAutoCommit;

    public TransactionHolder(Object transaction) {
        this.transaction = transaction;
        this.mustRestoreAutoCommit = false;
    }

    public Object getTransaction() {
        return transaction;
    }

    public boolean isMustRestoreAutoCommit() {
        return this.mustRestoreAutoCommit;
    }

    public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
        this.mustRestoreAutoCommit = mustRestoreAutoCommit;
    }
}
