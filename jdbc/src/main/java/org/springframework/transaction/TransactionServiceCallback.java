package org.springframework.transaction;

@FunctionalInterface
public interface TransactionServiceCallback<T> {

    T call();
}
