package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction(Connection connection, ConnectionCallBack<T> action) throws SQLException;
}
