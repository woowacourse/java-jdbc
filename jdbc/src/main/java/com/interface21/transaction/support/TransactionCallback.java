package com.interface21.transaction.support;

public interface TransactionCallback<T> {
    T doInTransaction();
}
