package org.springframework.transaction.support;

@FunctionalInterface
public interface FunctionForObject<T> {

    T service();
}
