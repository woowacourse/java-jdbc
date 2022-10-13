package com.techcourse.service.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {

    T call();
}
