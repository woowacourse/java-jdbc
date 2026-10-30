package com.interface21.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {

    T doInTransaction();
}
