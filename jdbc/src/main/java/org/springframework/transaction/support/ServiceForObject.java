package org.springframework.transaction.support;

@FunctionalInterface
public interface ServiceForObject<T> {

    T service();
}
