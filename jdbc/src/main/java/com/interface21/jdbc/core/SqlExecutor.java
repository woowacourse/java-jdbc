package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecutor<T> {
    T execute(Connection conn) throws SQLException;
}
