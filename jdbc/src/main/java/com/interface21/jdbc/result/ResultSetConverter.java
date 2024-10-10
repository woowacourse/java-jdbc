package com.interface21.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetConverter<T, R> {
    R convert(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException;
}
