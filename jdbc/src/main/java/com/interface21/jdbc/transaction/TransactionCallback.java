package com.interface21.jdbc.transaction;

@FunctionalInterface
public interface TransactionCallback {
    void execute();
}
