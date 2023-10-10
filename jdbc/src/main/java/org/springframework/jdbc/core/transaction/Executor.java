package org.springframework.jdbc.core.transaction;

@FunctionalInterface
public interface Executor<T> {

    T execute();
}
