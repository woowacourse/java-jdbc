package com.interface21.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    T mapRowToResult(final ResultSet rs) throws SQLException;
}
