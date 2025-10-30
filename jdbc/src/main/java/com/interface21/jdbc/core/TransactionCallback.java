package com.interface21.jdbc.core;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction(Connection connection) throws Exception;
}
