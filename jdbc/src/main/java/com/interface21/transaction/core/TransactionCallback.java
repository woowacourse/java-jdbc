package com.interface21.transaction.core;

import javax.annotation.Nullable;

@FunctionalInterface
public interface TransactionCallback<T> {

    @Nullable
    T doInTransaction();
}
