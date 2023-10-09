package org.springframework.transaction;

@FunctionalInterface
public interface TransactionExecutor<T> {

    T execute();
}
