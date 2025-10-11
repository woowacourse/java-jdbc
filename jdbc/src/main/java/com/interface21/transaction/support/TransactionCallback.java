package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback<T> {
    T execute(Connection connection) throws SQLException;
}
