package com.interface21.jdbc.core;

import javax.annotation.Nullable;

@FunctionalInterface
public interface TransactionCallback<T> {

    @Nullable
    T doInTransaction();
}
