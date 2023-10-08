package org.springframework.jdbc.core;

@FunctionalInterface
public interface TransactionExecutor<T> {

    T execute();
}
