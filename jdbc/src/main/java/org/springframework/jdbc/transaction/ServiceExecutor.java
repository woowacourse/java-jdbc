package org.springframework.jdbc.transaction;

@FunctionalInterface
public interface ServiceExecutor<T> {

    T execute();
}
