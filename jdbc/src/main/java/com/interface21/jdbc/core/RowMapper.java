package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    /** Maps the current row; JdbcTemplate advances the cursor and closes the result set. */
    T mapRow(ResultSet resultSet) throws SQLException;
}
