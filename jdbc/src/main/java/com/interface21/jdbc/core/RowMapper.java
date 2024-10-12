package com.interface21.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<ResultSet, T> {

    T mapRow(ResultSet rs) throws SQLException;
}
