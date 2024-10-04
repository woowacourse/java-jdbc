package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
interface ResultSetHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}
