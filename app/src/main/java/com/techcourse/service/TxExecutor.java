package com.techcourse.service;

@FunctionalInterface
public interface TxExecutor<T> {
    T execute();

}
