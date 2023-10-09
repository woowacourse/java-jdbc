package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionTemplate<T> {

    T run();
}
