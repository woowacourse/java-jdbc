package com.techcourse.service;

@FunctionalInterface
public interface TransactionExecutor<T> {

    T execute();
}
