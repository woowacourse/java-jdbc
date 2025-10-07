package com.interface21.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface QueryResultMapper<R> {

    R map(ResultSet resultSet) throws SQLException;
}
