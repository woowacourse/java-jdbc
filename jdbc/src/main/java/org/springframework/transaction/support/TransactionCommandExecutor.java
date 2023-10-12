package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionCommandExecutor {

    void run();
}
