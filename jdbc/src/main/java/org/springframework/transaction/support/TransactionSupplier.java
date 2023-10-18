package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionSupplier<T> {
    T get();
}
