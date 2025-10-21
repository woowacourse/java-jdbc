package com.interface21.transaction.support;

import javax.annotation.Nullable;

@FunctionalInterface
public interface TransactionCallback<T> {

    @Nullable
    T doInTransaction();
}
