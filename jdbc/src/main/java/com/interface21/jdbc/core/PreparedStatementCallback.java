package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface PreparedStatementCallback<T> {
    T doInPreparedStatement(PreparedStatement statement) throws SQLException;
}
