package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionCallback<T> {

    T execute();
}
