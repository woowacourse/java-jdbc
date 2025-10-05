package com.interface21.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface QueryResultSetMapper<R> {

    R map(ResultSet resultSet) throws SQLException;
}
