package com.interface21.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {
    T mapToRow(ResultSet resultSet) throws SQLException;
}
