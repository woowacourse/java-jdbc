package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionServiceExecutor<T> {

    T execute();
}
