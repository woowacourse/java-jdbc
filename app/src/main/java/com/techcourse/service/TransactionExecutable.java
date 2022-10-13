package com.techcourse.service;

@FunctionalInterface
public interface TransactionExecutable<T> {

    T execute();
}
