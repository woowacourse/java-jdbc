package com.techcourse.service;

@FunctionalInterface
public interface TransactionCallback<T> {

    T execute();
}
