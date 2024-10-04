package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface SqlExecutor<T> {
    T execute(PreparedStatement statement) throws SQLException;
}
