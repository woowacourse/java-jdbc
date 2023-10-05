package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionCallback<T> {

    @Nullable
    T execute();
}
