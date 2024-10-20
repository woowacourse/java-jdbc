package com.interface21.jdbc.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    T mapping(final ResultSet resultSet) throws SQLException;
}
