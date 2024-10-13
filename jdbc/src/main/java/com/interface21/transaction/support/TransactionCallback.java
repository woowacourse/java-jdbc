package com.interface21.transaction.support;

@FunctionalInterface
public interface TransactionCallback<T> {

    T doInTransaction();
}
