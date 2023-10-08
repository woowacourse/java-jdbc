package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionExecutor {
    void doGetTransaction();
}
