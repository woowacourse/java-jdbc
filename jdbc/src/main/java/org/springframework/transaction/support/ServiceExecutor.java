package org.springframework.transaction.support;

@FunctionalInterface
public interface ServiceExecutor<T> {
    T doGetTransaction();
}
