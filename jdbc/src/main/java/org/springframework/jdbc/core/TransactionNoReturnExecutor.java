package org.springframework.jdbc.core;

@FunctionalInterface
public interface TransactionNoReturnExecutor {

    void execute();
}
