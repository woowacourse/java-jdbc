package org.springframework.transaction.support;

public interface TransactionExecutor<T> {

    T execute();
}
