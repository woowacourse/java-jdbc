package org.springframework.transaction;

@FunctionalInterface
public interface TransactionalCallbackWithReturnValue<T> {

    T execute();
}
