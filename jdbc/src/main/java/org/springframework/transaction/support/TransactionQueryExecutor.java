package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionQueryExecutor<T> {

    T get();
}
