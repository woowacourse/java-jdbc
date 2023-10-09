package org.springframework.transaction;

@FunctionalInterface
public interface TransactionExecutor {

    void execute();
}
