package org.springframework.transaction;

@FunctionalInterface
public interface ConnectionAction<T> {

    T execute();
}
