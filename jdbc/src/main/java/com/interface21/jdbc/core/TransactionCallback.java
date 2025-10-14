package com.interface21.jdbc.core;

import java.sql.Connection;
import javax.annotation.Nullable;

@FunctionalInterface
public interface TransactionCallback<T> {

    @Nullable
    T doInTransaction(Connection conn);
}
