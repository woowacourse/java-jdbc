package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionNoReturnExecutor {

    void execute();
}
