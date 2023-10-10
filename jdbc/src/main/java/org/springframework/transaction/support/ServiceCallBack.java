package org.springframework.transaction.support;

@FunctionalInterface
public interface ServiceCallBack<T> {
    T execute();
}
