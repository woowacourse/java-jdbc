package org.springframework.transaction.support;

@FunctionalInterface
public interface BusinessLogicProcessor<T> {

    T process();
}
