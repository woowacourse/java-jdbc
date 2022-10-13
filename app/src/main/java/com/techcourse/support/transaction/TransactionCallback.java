package com.techcourse.support.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {

    T call();
}
