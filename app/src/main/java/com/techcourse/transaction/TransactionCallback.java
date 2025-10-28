package com.techcourse.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction();
}
