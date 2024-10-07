package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

interface QueryExecutor<T> {
    T execute(PreparedStatement preparedStatement) throws SQLException;

    default void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setString(i + 1, String.valueOf(parameters[i]));
        }
    }
}
