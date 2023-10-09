package org.springframework.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {

    T execute();
}
