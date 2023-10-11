package org.springframework.transaction.support;

public interface TransactionExecution<T> {
    T execute();
}
