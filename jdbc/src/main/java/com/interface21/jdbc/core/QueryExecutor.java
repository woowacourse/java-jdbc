package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface QueryExecutor<T> {

    T execute(PreparedStatement preparedStatement) throws SQLException;

    static void setArguments(final Object[] args, final PreparedStatement statement) throws SQLException {
        int index = 1;
        for (final Object arg : args) {
            statement.setObject(index++, arg);
        }
    }
}
