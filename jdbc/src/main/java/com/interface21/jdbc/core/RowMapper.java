package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    /** Maps the current row without advancing the cursor; rowNum starts at zero. */
    T mapRow(ResultSet resultSet, int rowNum) throws SQLException;
}
