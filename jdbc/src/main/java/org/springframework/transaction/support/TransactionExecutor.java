package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionExecutor<T> {

    T action();

}
