package com.techcourse.service.transaction;

@FunctionalInterface
public interface TransactionQueryExecutor<T> {

    T get();
}
