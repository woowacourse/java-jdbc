package org.springframework.transaction;

@FunctionalInterface
public interface TransactionAction<T> {

    T execute();
}
