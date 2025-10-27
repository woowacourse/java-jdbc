package com.interface21.jdbc.core;

@FunctionalInterface
public interface TransactionCallback<T> {

    T doInTransaction();
}
