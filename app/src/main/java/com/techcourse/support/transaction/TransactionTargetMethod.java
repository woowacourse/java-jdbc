package com.techcourse.support.transaction;

@FunctionalInterface
public interface TransactionTargetMethod<T> {

    T call();
}
